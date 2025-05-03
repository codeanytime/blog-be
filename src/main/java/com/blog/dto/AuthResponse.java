package com.blog.dto;

import com.blog.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;

    private Object user;

    private String error;

    public AuthResponse() {
    }

    public AuthResponse(String token, Object user, String error) {
        this.token = token;
        this.user = user;
        this.error = error;
    }

    // Static factory method for User objects
    public static AuthResponse fromUser(String token, User user, String error) {
        AuthResponse response = new AuthResponse();
        response.token = token;
        if (user != null) {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setPictureUrl(user.getPictureUrl());
            dto.setRole(user.getRole());
            dto.setRoles(new String[]{user.getRole()});
            response.user = dto;
        }
        response.error = error;
        return response;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
