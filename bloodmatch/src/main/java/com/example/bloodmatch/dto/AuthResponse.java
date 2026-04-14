package com.example.bloodmatch.dto;

public class AuthResponse {
    private String id;
    private String role;
    private String name;
    private String email;

    public AuthResponse(String id, String role, String name, String email) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
