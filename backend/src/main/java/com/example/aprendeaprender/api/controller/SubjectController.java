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

        if (request.topics() != null) {
            for (int i = 0; i < request.topics().size(); i++) {
                jdbc.sql("INSERT INTO subject_topic(subject_id, position, topic) VALUES (:id, :position, :topic)")
                        .param("id", id).param("position", i).param("topic", request.topics().get(i)).update();
            }
        }
        return findOne(userId, id);
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
}
