package com.example.aprendeaprender.api.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private final JdbcClient jdbc;

    public SubjectController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<SubjectResponse> list(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return jdbc.sql("""
                SELECT id, name, instructor, utadeo_id, created_at
                FROM subject WHERE user_id = :userId ORDER BY name
                """)
                .param("userId", userId)
                .query((rs, n) -> new SubjectResponse(
                        rs.getObject("id", UUID.class), rs.getString("name"),
                        rs.getString("instructor"), (Integer) rs.getObject("utadeo_id"),
                        rs.getObject("created_at", OffsetDateTime.class)))
                .list();
    }

    @PostMapping
    @Transactional
    public SubjectResponse create(Authentication auth, @RequestBody CreateSubjectRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        UUID id = jdbc.sql("""
                INSERT INTO subject(user_id, name, instructor, utadeo_id)
                VALUES (:userId, :name, :instructor, :utadeoId)
                RETURNING id
                """)
                .param("userId", userId)
                .param("name", request.name())
                .param("instructor", request.instructor())
                .param("utadeoId", request.utadeoId())
                .query(UUID.class)
                .single();
        replaceTopics(id, request.topics());
        return findOne(userId, id);
    }

    @PutMapping("/utadeo/sync")
    @Transactional
    public List<SubjectResponse> syncUtadeo(Authentication auth, @RequestBody List<UtadeoSubjectRequest> courses) {
        UUID userId = UUID.fromString(auth.getName());
        for (UtadeoSubjectRequest course : courses) {
            UUID subjectId = jdbc.sql("""
                    INSERT INTO subject(user_id, name, instructor, utadeo_id)
                    VALUES (:userId, :name, :instructor, :utadeoId)
                    ON CONFLICT (user_id, utadeo_id)
                    DO UPDATE SET name = EXCLUDED.name, instructor = EXCLUDED.instructor, updated_at = now()
                    RETURNING id
                    """)
                    .param("userId", userId)
                    .param("name", course.name())
                    .param("instructor", course.instructor())
                    .param("utadeoId", course.utadeoId())
                    .query(UUID.class)
                    .single();

            jdbc.sql("DELETE FROM subject_participant WHERE subject_id = :subjectId")
                    .param("subjectId", subjectId).update();
            if (course.participants() != null) {
                for (ParticipantRequest participant : course.participants()) {
                    jdbc.sql("""
                            INSERT INTO subject_participant(subject_id, name, role)
                            VALUES (:subjectId, :name, :role)
                            """)
                            .param("subjectId", subjectId)
                            .param("name", participant.name())
                            .param("role", participant.role() == null ? "" : participant.role())
                            .update();
                }
            }
        }
        return list(auth);
    }

    @GetMapping("/{id}/participants")
    public List<ParticipantResponse> participants(Authentication auth, @PathVariable UUID id) {
        UUID userId = UUID.fromString(auth.getName());
        return jdbc.sql("""
                SELECT p.name, p.role
                FROM subject_participant p
                JOIN subject s ON s.id = p.subject_id
                WHERE p.subject_id = :id AND s.user_id = :userId
                ORDER BY p.name
                """)
                .param("id", id).param("userId", userId)
                .query((rs, n) -> new ParticipantResponse(rs.getString("name"), rs.getString("role")))
                .list();
    }

    @GetMapping("/{id}")
    public SubjectResponse get(Authentication auth, @PathVariable UUID id) {
        return findOne(UUID.fromString(auth.getName()), id);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(Authentication auth, @PathVariable UUID id) {
        jdbc.sql("DELETE FROM subject WHERE id = :id AND user_id = :userId")
                .param("id", id).param("userId", UUID.fromString(auth.getName())).update();
    }

    private void replaceTopics(UUID subjectId, List<String> topics) {
        jdbc.sql("DELETE FROM subject_topic WHERE subject_id = :id").param("id", subjectId).update();
        if (topics == null) return;
        for (int i = 0; i < topics.size(); i++) {
            String topic = topics.get(i) == null ? "" : topics.get(i).trim();
            if (topic.isBlank()) continue;
            jdbc.sql("INSERT INTO subject_topic(subject_id, position, topic) VALUES (:id, :position, :topic)")
                    .param("id", subjectId).param("position", i).param("topic", topic).update();
        }
    }

    private SubjectResponse findOne(UUID userId, UUID id) {
        return jdbc.sql("""
                SELECT id, name, instructor, utadeo_id, created_at
                FROM subject WHERE id = :id AND user_id = :userId
                """)
                .param("id", id).param("userId", userId)
                .query((rs, n) -> new SubjectResponse(
                        rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("instructor"),
                        (Integer) rs.getObject("utadeo_id"), rs.getObject("created_at", OffsetDateTime.class)))
                .single();
    }

    public record CreateSubjectRequest(String name, String instructor, Integer utadeoId, List<String> topics) {}
    public record SubjectResponse(UUID id, String name, String instructor, Integer utadeoId, OffsetDateTime createdAt) {}
    public record ParticipantRequest(String name, String role) {}
    public record ParticipantResponse(String name, String role) {}
    public record UtadeoSubjectRequest(Integer utadeoId, String name, String instructor, List<ParticipantRequest> participants) {}
}
