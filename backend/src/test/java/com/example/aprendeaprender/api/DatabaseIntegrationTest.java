package com.example.aprendeaprender.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest
class DatabaseIntegrationTest {
    @Autowired
    JdbcClient jdbc;

    @Test
    void flywayCreaLasTablasPrincipales() {
        Integer count = jdbc.sql("""
                SELECT count(*) FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN ('app_user','auth_session','subject','task','daily_challenge')
                """)
                .query(Integer.class)
                .single();
        assertThat(count).isEqualTo(5);
    }
}
