package com.blog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;

    private String name;

    private String email;

    private String pictureUrl;

    private String role;

    public UserDTO(Long id, String name, String email, String pictureUrl, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.pictureUrl = pictureUrl;
        this.role = role;
    }
}
