package com.example.aprendeaprender.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fitness function M6: el contrato paginado conserva aislamiento y límites.
 * Datos de prueba reales en PostgreSQL, con rollback por test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskPaginationFitnessTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcClient jdbc;

    UUID alice;
    UUID bob;
    UUID aliceSubject;
    UUID bobSubject;
    UUID firstTask;
    UUID secondTask;
    UUID bobTask;

    @BeforeEach
    void seed() {
        alice = UUID.randomUUID();
        bob = UUID.randomUUID();
        aliceSubject = UUID.randomUUID();
        bobSubject = UUID.randomUUID();
        // Dos UUID consecutivos, no dependemos del orden firmado de UUID en Java.
        long suffix = java.util.concurrent.ThreadLocalRandom.current().nextLong(1L, Long.MAX_VALUE - 1);
        firstTask = new UUID(0L, suffix);
        secondTask = new UUID(0L, suffix + 1);
        bobTask = UUID.randomUUID();

        insertUser(alice);
        insertUser(bob);
        insertSubject(aliceSubject, alice);
        insertSubject(bobSubject, bob);

        OffsetDateTime due = OffsetDateTime.parse("2026-11-04T12:00:00Z");
        // Misma fecha y created_at: el ID desempata el orden entre páginas.
        OffsetDateTime created = OffsetDateTime.parse("2026-10-20T12:00:00Z");
        insertTask(firstTask, alice, aliceSubject, due, created);
        insertTask(secondTask, alice, aliceSubject, due, created);
        insertTask(bobTask, bob, bobSubject, due, created);
    }

    @Test
    void cadaUsuarioSoloVeSusPropiasTareas() throws Exception {
        mvc.perform(get("/api/tasks/page").with(user(alice.toString()))
                    .param("page", "0").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].subjectId").value(aliceSubject.toString()))
                .andExpect(jsonPath("$.items[1].subjectId").value(aliceSubject.toString()));

        mvc.perform(get("/api/tasks/page").with(user(bob.toString()))
                    .param("page", "0").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(bobTask.toString()));
    }

    @Test
    void limitaElTamanoYRechazaParametrosInvalidos() throws Exception {
        for (String[] invalid : new String[][] {
            {"-1", "50"}, {"0", "0"}, {"0", "101"},
            {"2147483647", "100"}
        }) {
            mvc.perform(get("/api/tasks/page").with(user(alice.toString()))
                        .param("page", invalid[0]).param("size", invalid[1]))
                    .andExpect(status().isBadRequest());
        }

        mvc.perform(get("/api/tasks/page").with(user(alice.toString()))
                    .param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    void paginasContiguasMantienenOrdenDeterminista() throws Exception {
        String first = firstTask.compareTo(secondTask) < 0 ? firstTask.toString() : secondTask.toString();
        String second = firstTask.compareTo(secondTask) < 0 ? secondTask.toString() : firstTask.toString();

        mvc.perform(get("/api/tasks/page").with(user(alice.toString()))
                    .param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(first))
                .andExpect(jsonPath("$.page").value(0));

        mvc.perform(get("/api/tasks/page").with(user(alice.toString()))
                    .param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(second))
                .andExpect(jsonPath("$.page").value(1));

        mvc.perform(get("/api/tasks/page").with(user(alice.toString()))
                    .param("page", "2").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void sinSesionNoPuedeConsultarTareas() throws Exception {
        mvc.perform(get("/api/tasks/page")).andExpect(status().isForbidden());
    }

    private void insertUser(UUID id) {
        jdbc.sql("""
                INSERT INTO app_user(id, email, password_hash, first_name, last_name)
                VALUES (:id, :email, 'test-only', 'Fitness', 'M6')
                """)
            .param("id", id)
            .param("email", id + "@fitness.local")
            .update();
    }

    private void insertSubject(UUID id, UUID userId) {
        jdbc.sql("INSERT INTO subject(id, user_id, name) VALUES (:id, :userId, 'Pruebas M6')")
            .param("id", id).param("userId", userId).update();
    }

    private void insertTask(UUID id, UUID userId, UUID subjectId,
                            OffsetDateTime due, OffsetDateTime created) {
        jdbc.sql("""
                INSERT INTO task(id, user_id, subject_id, title, due_at, created_at)
                VALUES (:id, :userId, :subjectId, 'Tarea fitness', :due, :created)
                """)
            .param("id", id).param("userId", userId).param("subjectId", subjectId)
            .param("due", due).param("created", created).update();
    }
}
