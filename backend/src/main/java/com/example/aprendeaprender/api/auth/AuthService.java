package com.example.aprendeaprender.api.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final JdbcClient jdbc;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();
    private final Duration sessionDuration;

    public AuthService(JdbcClient jdbc, PasswordEncoder passwordEncoder,
                       @Value("${app.auth.session-hours:24}") long sessionHours) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.sessionDuration = Duration.ofHours(sessionHours);
    }

    @Transactional
    public SessionResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        boolean exists = jdbc.sql("SELECT EXISTS(SELECT 1 FROM app_user WHERE email = :email)")
                .param("email", email)
                .query(Boolean.class)
                .single();
        if (exists) throw new IllegalArgumentException("El correo ya está registrado");

        UUID userId = jdbc.sql("""
                INSERT INTO app_user(email, password_hash, first_name, last_name, phone)
                VALUES (:email, :password, :firstName, :lastName, :phone)
                RETURNING id
                """)
                .param("email", email)
                .param("password", passwordEncoder.encode(request.password()))
                .param("firstName", request.firstName().trim())
                .param("lastName", request.lastName().trim())
                .param("phone", request.phone())
                .query(UUID.class)
                .single();
        return createSession(userId);
    }

    @Transactional
    public SessionResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        UserPassword row = jdbc.sql("SELECT id, password_hash FROM app_user WHERE email = :email")
                .param("email", email)
                .query((rs, n) -> new UserPassword(
                        rs.getObject("id", UUID.class), rs.getString("password_hash")))
                .optional()
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), row.passwordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        return createSession(row.id());
    }

    @Transactional
    public void logout(String rawToken) {
        jdbc.sql("DELETE FROM auth_session WHERE token_hash = :hash")
                .param("hash", hashToken(rawToken))
                .update();
    }

    public Optional<UUID> findUserIdByToken(String rawToken) {
        return jdbc.sql("""
                SELECT user_id FROM auth_session
                WHERE token_hash = :hash AND expires_at > now()
                """)
                .param("hash", hashToken(rawToken))
                .query(UUID.class)
                .optional();
    }

    public UserResponse currentUser(UUID userId) {
        return jdbc.sql("""
                SELECT id, email, first_name, last_name, phone
                FROM app_user WHERE id = :id
                """)
                .param("id", userId)
                .query((rs, n) -> new UserResponse(
                        rs.getObject("id", UUID.class), rs.getString("email"),
                        rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("phone")))
                .single();
    }

    private SessionResponse createSession(UUID userId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(sessionDuration);
        jdbc.sql("INSERT INTO auth_session(token_hash, user_id, expires_at) VALUES (:hash, :userId, :expiresAt)")
                .param("hash", hashToken(token))
                .param("userId", userId)
                .param("expiresAt", expiresAt)
                .update();
        return new SessionResponse(token, expiresAt, currentUser(userId));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo procesar el token", ex);
        }
    }

    private record UserPassword(UUID id, String passwordHash) {}

    public record RegisterRequest(String email, String password, String firstName, String lastName, String phone) {}
    public record LoginRequest(String email, String password) {}
    public record SessionResponse(String token, OffsetDateTime expiresAt, UserResponse user) {}
    public record UserResponse(UUID id, String email, String firstName, String lastName, String phone) {}
}
