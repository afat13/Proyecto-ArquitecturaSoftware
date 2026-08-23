package com.example.aprendeaprender.api.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final JdbcClient jdbc;

    public TaskController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<TaskResponse> list(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return jdbc.sql("""
                SELECT t.id, t.subject_id, s.name AS subject_name, t.title, t.description,
                       t.due_at, t.priority, t.status, t.created_at
                FROM task t
                JOIN subject s ON s.id = t.subject_id
                WHERE t.user_id = :userId
                ORDER BY t.due_at NULLS LAST, t.created_at DESC
                """)
                .param("userId", userId)
                .query((rs, n) -> new TaskResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("subject_id", UUID.class),
                        rs.getString("subject_name"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getObject("due_at", OffsetDateTime.class),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getObject("created_at", OffsetDateTime.class)))
                .list();
    }

    @PostMapping
    @Transactional
    public TaskResponse create(Authentication auth, @RequestBody CreateTaskRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        UUID id = jdbc.sql("""
                INSERT INTO task(user_id, subject_id, title, description, due_at, priority, status)
                SELECT :userId, s.id, :title, :description, :dueAt, :priority, :status
                FROM subject s WHERE s.id = :subjectId AND s.user_id = :userId
                RETURNING id
                """)
                .param("userId", userId)
                .param("subjectId", request.subjectId())
                .param("title", request.title())
                .param("description", request.description() == null ? "" : request.description())
                .param("dueAt", request.dueAt())
                .param("priority", request.priority() == null ? "MEDIA" : request.priority())
                .param("status", request.status() == null ? "PENDIENTE" : request.status())
                .query(UUID.class)
                .single();
        return findOne(userId, id);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public TaskResponse updateStatus(Authentication auth, @PathVariable UUID id, @RequestBody StatusRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        jdbc.sql("UPDATE task SET status = :status, updated_at = now() WHERE id = :id AND user_id = :userId")
                .param("status", request.status())
                .param("id", id)
                .param("userId", userId)
                .update();
        return findOne(userId, id);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(Authentication auth, @PathVariable UUID id) {
        jdbc.sql("DELETE FROM task WHERE id = :id AND user_id = :userId")
                .param("id", id)
                .param("userId", UUID.fromString(auth.getName()))
                .update();
    }

    private TaskResponse findOne(UUID userId, UUID id) {
        return jdbc.sql("""
                SELECT t.id, t.subject_id, s.name AS subject_name, t.title, t.description,
                       t.due_at, t.priority, t.status, t.created_at
                FROM task t JOIN subject s ON s.id = t.subject_id
                WHERE t.id = :id AND t.user_id = :userId
                """)
                .param("id", id)
                .param("userId", userId)
                .query((rs, n) -> new TaskResponse(
                        rs.getObject("id", UUID.class), rs.getObject("subject_id", UUID.class),
                        rs.getString("subject_name"), rs.getString("title"), rs.getString("description"),
                        rs.getObject("due_at", OffsetDateTime.class), rs.getString("priority"),
                        rs.getString("status"), rs.getObject("created_at", OffsetDateTime.class)))
                .single();
    }

    public record CreateTaskRequest(UUID subjectId, String title, String description, OffsetDateTime dueAt,
                                    String priority, String status) {}
    public record StatusRequest(String status) {}
    public record TaskResponse(UUID id, UUID subjectId, String subjectName, String title, String description,
                               OffsetDateTime dueAt, String priority, String status, OffsetDateTime createdAt) {}
}
