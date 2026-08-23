package com.example.aprendeaprender.api.controller;

import java.util.UUID;

import com.example.aprendeaprender.api.auth.AuthService.UserResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final JdbcClient jdbc;

    public ProfileController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public UserResponse get(Authentication auth) {
        return find(UUID.fromString(auth.getName()));
    }

    @PatchMapping
    @Transactional
    public UserResponse update(Authentication auth, @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        jdbc.sql("""
                UPDATE app_user SET
                    first_name = COALESCE(:firstName, first_name),
                    last_name = COALESCE(:lastName, last_name),
                    phone = COALESCE(:phone, phone),
                    updated_at = now()
                WHERE id = :id
                """)
                .param("firstName", request.firstName())
                .param("lastName", request.lastName())
                .param("phone", request.phone())
                .param("id", userId)
                .update();
        return find(userId);
    }

    private UserResponse find(UUID id) {
        return jdbc.sql("SELECT id, email, first_name, last_name, phone FROM app_user WHERE id = :id")
                .param("id", id)
                .query((rs, n) -> new UserResponse(
                        rs.getObject("id", UUID.class), rs.getString("email"),
                        rs.getString("first_name"), rs.getString("last_name"), rs.getString("phone")))
                .single();
    }

    public record UpdateProfileRequest(String firstName, String lastName, String phone) {}
}
