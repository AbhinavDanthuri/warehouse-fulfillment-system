package com.abhinav.warehouse.security;

public record AuthResponse(String token, String email, String role) {}
