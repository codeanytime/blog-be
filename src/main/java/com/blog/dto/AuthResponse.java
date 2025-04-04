package com.blog.dto;

import com.blog.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;

    private Object user;

    private String error;

    public AuthResponse(String token, User user, String error) {
        this.token = token;
        if (user != null) {
            this.user = new UserDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPictureUrl(),
                    user.getRole()
            );
        }
        this.error = error;
    }
}

