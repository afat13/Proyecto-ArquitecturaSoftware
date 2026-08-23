package com.example.aprendeaprender.api.controller;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/utadeo/sync")
    @Transactional
    public List<TaskResponse> syncUtadeo(Authentication auth, @RequestBody List<UtadeoTaskRequest> assignments) {
        UUID userId = UUID.fromString(auth.getName());
        for (UtadeoTaskRequest assignment : assignments) {
            UUID subjectId = jdbc.sql("""
                    SELECT id FROM subject WHERE user_id = :userId AND utadeo_id = :courseId
                    """)
                    .param("userId", userId)
                    .param("courseId", assignment.courseId())
                    .query(UUID.class)
                    .optional()
                    .orElse(null);
            if (subjectId == null) continue;

            OffsetDateTime dueAt = assignment.dueDateMillis() > 0
                    ? OffsetDateTime.ofInstant(Instant.ofEpochMilli(assignment.dueDateMillis()), ZoneOffset.UTC)
                    : null;
            String externalId = String.valueOf(assignment.assignmentId());
            String status = normalizeStatus(assignment.status());

            jdbc.sql("""
                    INSERT INTO task(user_id, subject_id, title, description, due_at, priority, status, utadeo_assignment_id)
                    VALUES (:userId, :subjectId, :title, :description, :dueAt, 'MEDIA', :status, :externalId)
                    ON CONFLICT (user_id, utadeo_assignment_id) WHERE utadeo_assignment_id IS NOT NULL
                    DO UPDATE SET subject_id = EXCLUDED.subject_id, title = EXCLUDED.title,
                                  description = EXCLUDED.description, due_at = EXCLUDED.due_at,
                                  status = EXCLUDED.status, updated_at = now()
                    """)
                    .param("userId", userId)
                    .param("subjectId", subjectId)
                    .param("title", assignment.title())
                    .param("description", assignment.description() == null ? "" : assignment.description())
                    .param("dueAt", dueAt)
                    .param("status", status)
                    .param("externalId", externalId)
                    .update();
        }
        return list(auth);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public TaskResponse updateStatus(Authentication auth, @PathVariable UUID id, @RequestBody StatusRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        jdbc.sql("UPDATE task SET status = :status, updated_at = now() WHERE id = :id AND user_id = :userId")
                .param("status", normalizeStatus(request.status()))
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

    private String normalizeStatus(String value) {
        if (value == null) return "PENDIENTE";
        return switch (value.toUpperCase()) {
            case "COMPLETADA", "COMPLETADO", "SUBMITTED", "GRADED" -> "COMPLETADA";
            case "EN_PROGRESO", "EN PROGRESO", "DRAFT" -> "EN_PROGRESO";
            default -> "PENDIENTE";
        };
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
    public record UtadeoTaskRequest(int assignmentId, int courseId, String title, String description,
                                    long dueDateMillis, String status) {}
    public record StatusRequest(String status) {}
    public record TaskResponse(UUID id, UUID subjectId, String subjectName, String title, String description,
                               OffsetDateTime dueAt, String priority, String status, OffsetDateTime createdAt) {}
}
