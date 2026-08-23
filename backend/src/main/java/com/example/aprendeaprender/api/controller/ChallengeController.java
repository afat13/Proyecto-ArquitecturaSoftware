package com.example.aprendeaprender.api.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/month/{yearMonth}")
    public List<Integer> completedDays(Authentication auth, @PathVariable String yearMonth) {
        UUID userId = UUID.fromString(auth.getName());
        YearMonth month = YearMonth.parse(yearMonth);
        return jdbc.sql("""
                SELECT EXTRACT(DAY FROM challenge_date)::int AS day
                FROM daily_challenge
                WHERE user_id = :userId
                  AND completed = TRUE
                  AND challenge_date BETWEEN :start AND :end
                ORDER BY challenge_date
                """)
                .param("userId", userId)
                .param("start", month.atDay(1))
                .param("end", month.atEndOfMonth())
                .query(Integer.class)
                .list();
    }

    @GetMapping("/today/subjects/{subjectId}/questions")
    public List<QuestionResponse> questions(Authentication auth, @PathVariable UUID subjectId) {
        UUID userId = UUID.fromString(auth.getName());
        return jdbc.sql("""
                SELECT q.id, q.subject_id, s.name AS subject_name, q.question,
                       q.option_a, q.option_b, q.option_c, q.option_d,
                       q.correct_option, q.explanation
                FROM challenge_question q
                JOIN daily_challenge d ON d.id = q.challenge_id
                JOIN subject s ON s.id = q.subject_id
                WHERE d.user_id = :userId AND d.challenge_date = CURRENT_DATE
                  AND q.subject_id = :subjectId
                ORDER BY q.created_at, q.id
                """)
                .param("userId", userId)
                .param("subjectId", subjectId)
                .query((rs, n) -> new QuestionResponse(
                        rs.getObject("id", UUID.class), rs.getObject("subject_id", UUID.class),
                        rs.getString("subject_name"), rs.getString("question"),
                        List.of(rs.getString("option_a"), rs.getString("option_b"),
                                rs.getString("option_c"), rs.getString("option_d")),
                        rs.getInt("correct_option"), rs.getString("explanation")))
                .list();
    }

    @PutMapping("/today/subjects/{subjectId}/questions")
    @Transactional
    public List<QuestionResponse> saveQuestions(Authentication auth, @PathVariable UUID subjectId,
                                                 @RequestBody List<QuestionRequest> questions) {
        UUID userId = UUID.fromString(auth.getName());
        DailyChallengeResponse current = today(auth);
        boolean ownsSubject = jdbc.sql("SELECT EXISTS(SELECT 1 FROM subject WHERE id = :id AND user_id = :userId)")
                .param("id", subjectId).param("userId", userId).query(Boolean.class).single();
        if (!ownsSubject) throw new IllegalArgumentException("La materia no pertenece al usuario autenticado");

        jdbc.sql("DELETE FROM challenge_question WHERE challenge_id = :challengeId AND subject_id = :subjectId")
                .param("challengeId", current.id()).param("subjectId", subjectId).update();

        for (QuestionRequest question : questions) {
            if (question.options() == null || question.options().size() != 4) continue;
            jdbc.sql("""
                    INSERT INTO challenge_question(
                        challenge_id, subject_id, question, option_a, option_b, option_c, option_d,
                        correct_option, explanation
                    ) VALUES (
                        :challengeId, :subjectId, :question, :a, :b, :c, :d, :correct, :explanation
                    )
                    """)
                    .param("challengeId", current.id())
                    .param("subjectId", subjectId)
                    .param("question", question.question())
                    .param("a", question.options().get(0))
                    .param("b", question.options().get(1))
                    .param("c", question.options().get(2))
                    .param("d", question.options().get(3))
                    .param("correct", question.correctOption())
                    .param("explanation", question.explanation() == null ? "" : question.explanation())
                    .update();
        }
        return questions(auth, subjectId);
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
    public record QuestionRequest(String question, List<String> options, int correctOption, String explanation) {}
    public record QuestionResponse(UUID id, UUID subjectId, String subjectName, String question,
                                   List<String> options, int correctOption, String explanation) {}
}
