package org.homework.dto.request;

import lombok.Data;

/**
 * DTO for authentication request (login).
 */
@Data
public class AuthRequest {
    private String username;
    private String password;
}