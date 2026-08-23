package com.example.aprendeaprender.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    private final JdbcClient jdbc;

    public ChallengeController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/today")
    @Transactional
    public DailyChallengeResponse today(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        LocalDate today = LocalDate.now();
        UUID challengeId = jdbc.sql("""
                INSERT INTO daily_challenge(user_id, challenge_date, total_subjects)
                VALUES (:userId, :date, (SELECT count(*) FROM subject WHERE user_id = :userId))
                ON CONFLICT (user_id, challenge_date)
                DO UPDATE SET total_subjects = EXCLUDED.total_subjects, updated_at = now()
                RETURNING id
                """)
                .param("userId", userId)
                .param("date", today)
                .query(UUID.class)
                .single();

        jdbc.sql("""
                INSERT INTO challenge_subject_completion(challenge_id, subject_id)
                SELECT :challengeId, id FROM subject WHERE user_id = :userId
                ON CONFLICT DO NOTHING
                """)
                .param("challengeId", challengeId)
                .param("userId", userId)
                .update();

        return load(challengeId, userId, today);
    }

    @PostMapping("/today/subjects/{subjectId}/complete")
    @Transactional
    public DailyChallengeResponse complete(Authentication auth, @PathVariable UUID subjectId) {
        UUID userId = UUID.fromString(auth.getName());
        DailyChallengeResponse current = today(auth);
        jdbc.sql("""
                UPDATE challenge_subject_completion c
                SET completed = TRUE, completed_at = now()
                WHERE challenge_id = :challengeId AND subject_id = :subjectId
                  AND EXISTS (SELECT 1 FROM subject s WHERE s.id = :subjectId AND s.user_id = :userId)
                """)
                .param("challengeId", current.id())
                .param("subjectId", subjectId)
                .param("userId", userId)
                .update();

        jdbc.sql("""
                UPDATE daily_challenge d SET completed = NOT EXISTS (
                    SELECT 1 FROM challenge_subject_completion c
                    WHERE c.challenge_id = d.id AND c.completed = FALSE
                ), updated_at = now()
                WHERE d.id = :challengeId
                """)
                .param("challengeId", current.id())
                .update();
        return load(current.id(), userId, LocalDate.now());
    }

    private DailyChallengeResponse load(UUID id, UUID userId, LocalDate date) {
        List<SubjectCompletion> subjects = jdbc.sql("""
                SELECT s.id, s.name, c.completed
                FROM challenge_subject_completion c
                JOIN subject s ON s.id = c.subject_id
                WHERE c.challenge_id = :id AND s.user_id = :userId
                ORDER BY s.name
                """)
                .param("id", id).param("userId", userId)
                .query((rs, n) -> new SubjectCompletion(
                        rs.getObject("id", UUID.class), rs.getString("name"), rs.getBoolean("completed")))
                .list();
        boolean completed = subjects.stream().allMatch(SubjectCompletion::completed) && !subjects.isEmpty();
        return new DailyChallengeResponse(id, date, subjects.size(), completed, subjects);
    }

    public record SubjectCompletion(UUID subjectId, String subjectName, boolean completed) {}
    public record DailyChallengeResponse(UUID id, LocalDate date, int totalSubjects, boolean completed,
                                         List<SubjectCompletion> subjects) {}
}
