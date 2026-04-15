package org.homework.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * DTO for user registration request.
 */
@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*]).+$",
            message = "Password must contain at least one digit, one uppercase letter and one special character")
    private String password;
}