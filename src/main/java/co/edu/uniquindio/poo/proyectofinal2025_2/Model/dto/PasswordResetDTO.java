package co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for password reset operations.
 * <p>
 * Contains information needed to identify a user and verify their identity
 * for password reset purposes, including security questions or verification codes.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDTO {

    /**
     * Email address of the user requesting password reset.
     */
    private String email;

    /**
     * Phone number for additional verification (optional).
     */
    private String phone;

    /**
     * Name for verification purposes.
     */
    private String name;

    /**
     * New password to be set.
     */
    private String newPassword;

    /**
     * Confirmation of the new password.
     */
    private String confirmPassword;

    /**
     * Verification code sent to user (for future implementation).
     */
    private String verificationCode;

    /**
     * Timestamp when the reset was requested.
     */
    private LocalDateTime requestedAt;

    /**
     * Whether the reset request has been verified.
     */
    private boolean verified;

    /**
     * Constructor for initial password reset request.
     *
     * @param email The user's email address
     */
    public PasswordResetDTO(String email) {
        this.email = email;
        this.requestedAt = LocalDateTime.now();
        this.verified = false;
    }

    /**
     * Checks if passwords match.
     *
     * @return True if newPassword equals confirmPassword
     */
    public boolean passwordsMatch() {
        if (newPassword == null || confirmPassword == null) {
            return false;
        }
        return newPassword.equals(confirmPassword);
    }

    /**
     * Validates that all required fields for password reset are present.
     *
     * @return True if all required fields are filled
     */
    public boolean isComplete() {
        return email != null && !email.trim().isEmpty() &&
               name != null && !name.trim().isEmpty() &&
               newPassword != null && !newPassword.trim().isEmpty() &&
               confirmPassword != null && !confirmPassword.trim().isEmpty();
    }
}
