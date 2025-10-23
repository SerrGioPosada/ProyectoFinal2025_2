package co.edu.uniquindio.poo.proyectofinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.PasswordResetDTO;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.PasswordUtility;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.PasswordValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling password reset operations.
 * <p>
 * Provides business logic for validating user identity, resetting passwords,
 * and managing password reset workflows across all user types (User, Admin, DeliveryPerson).
 * </p>
 */
public class PasswordResetService {

    // =========================================================================================
    // DEPENDENCIES
    // =========================================================================================

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;

    // =========================================================================================
    // CONSTRUCTORS
    // =========================================================================================

    /**
     * Constructor with dependency injection for testing.
     *
     * @param userRepository The user repository instance
     * @param adminRepository The admin repository instance
     * @param deliveryPersonRepository The delivery person repository instance
     */
    public PasswordResetService(UserRepository userRepository,
                                AdminRepository adminRepository,
                                DeliveryPersonRepository deliveryPersonRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.deliveryPersonRepository = deliveryPersonRepository;
    }

    /**
     * Default constructor using singleton repositories.
     */
    public PasswordResetService() {
        this(UserRepository.getInstance(),
             AdminRepository.getInstance(),
             DeliveryPersonRepository.getInstance());
    }

    // =========================================================================================
    // PASSWORD RESET OPERATIONS
    // =========================================================================================

    /**
     * Verifies if a user exists and their identity matches the provided information.
     * <p>
     * This method checks if the email exists and validates additional information
     * like name or phone to verify the user's identity before allowing password reset.
     * </p>
     *
     * @param resetDTO The password reset DTO with email and verification info
     * @return VerificationResult with success status and messages
     */
    public VerificationResult verifyUserIdentity(PasswordResetDTO resetDTO) {
        Logger.info("Verifying user identity for password reset: " + resetDTO.getEmail());

        if (resetDTO.getEmail() == null || resetDTO.getEmail().trim().isEmpty()) {
            return new VerificationResult(false, "El email es requerido");
        }

        // Try to find user in all repositories
        AuthenticablePerson person = findPersonByEmail(resetDTO.getEmail());

        if (person == null) {
            Logger.warn("No user found with email: " + resetDTO.getEmail());
            return new VerificationResult(false, "No se encontró ningún usuario con ese email");
        }

        // Verify name matches
        if (resetDTO.getName() == null || resetDTO.getName().trim().isEmpty()) {
            return new VerificationResult(false, "El nombre es requerido para verificación");
        }

        String fullName = person.getName() + " " + person.getLastName();
        if (!fullName.toLowerCase().contains(resetDTO.getName().toLowerCase())) {
            Logger.warn("Name verification failed for email: " + resetDTO.getEmail());
            return new VerificationResult(false, "La información de verificación no coincide");
        }

        Logger.info("User identity verified successfully for: " + resetDTO.getEmail());
        return new VerificationResult(true, "Identidad verificada correctamente");
    }

    /**
     * Resets the password for a user after verification.
     * <p>
     * This method validates the new password strength, ensures passwords match,
     * and updates the password in the appropriate repository.
     * </p>
     *
     * @param resetDTO The password reset DTO with new password
     * @return ResetResult with success status and messages
     */
    public ResetResult resetPassword(PasswordResetDTO resetDTO) {
        Logger.info("Attempting password reset for: " + resetDTO.getEmail());

        // Validate DTO completeness
        if (!resetDTO.isComplete()) {
            return new ResetResult(false, "Todos los campos son requeridos");
        }

        // Validate passwords match
        if (!resetDTO.passwordsMatch()) {
            return new ResetResult(false, "Las contraseñas no coinciden");
        }

        // Validate password strength
        List<String> validationErrors = PasswordValidator.validatePassword(resetDTO.getNewPassword());
        if (!validationErrors.isEmpty()) {
            return new ResetResult(false, String.join("\n", validationErrors));
        }

        // Find the person
        AuthenticablePerson person = findPersonByEmail(resetDTO.getEmail());
        if (person == null) {
            return new ResetResult(false, "Usuario no encontrado");
        }

        // Hash the new password
        String hashedPassword = PasswordUtility.hashPassword(resetDTO.getNewPassword());

        // Update password based on person type
        boolean updated = updatePasswordForPerson(person, hashedPassword);

        if (updated) {
            Logger.info("Password reset successfully for: " + resetDTO.getEmail());
            return new ResetResult(true, "Contraseña actualizada correctamente");
        } else {
            Logger.error("Failed to save password reset for: " + resetDTO.getEmail());
            return new ResetResult(false, "Error al guardar la nueva contraseña");
        }
    }

    /**
     * Validates a password reset request without actually resetting the password.
     *
     * @param resetDTO The password reset DTO to validate
     * @return List of validation error messages (empty if valid)
     */
    public List<String> validateResetRequest(PasswordResetDTO resetDTO) {
        List<String> errors = new ArrayList<>();

        if (resetDTO.getEmail() == null || resetDTO.getEmail().trim().isEmpty()) {
            errors.add("El email es requerido");
        }

        if (resetDTO.getName() == null || resetDTO.getName().trim().isEmpty()) {
            errors.add("El nombre es requerido para verificación");
        }

        if (resetDTO.getNewPassword() == null || resetDTO.getNewPassword().trim().isEmpty()) {
            errors.add("La nueva contraseña es requerida");
        }

        if (resetDTO.getConfirmPassword() == null || resetDTO.getConfirmPassword().trim().isEmpty()) {
            errors.add("La confirmación de contraseña es requerida");
        }

        if (!resetDTO.passwordsMatch()) {
            errors.add("Las contraseñas no coinciden");
        }

        if (resetDTO.getNewPassword() != null) {
            errors.addAll(PasswordValidator.validatePassword(resetDTO.getNewPassword()));
        }

        return errors;
    }

    // =========================================================================================
    // HELPER METHODS
    // =========================================================================================

    /**
     * Finds a person by email across all repositories.
     *
     * @param email The email to search for
     * @return The found person or null if not found
     */
    private AuthenticablePerson findPersonByEmail(String email) {
        // Check User repository
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // Check Admin repository
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            return adminOpt.get();
        }

        // Check DeliveryPerson repository
        Optional<DeliveryPerson> deliveryOpt = deliveryPersonRepository.findDeliveryPersonByEmail(email);
        return deliveryOpt.orElse(null);
    }

    /**
     * Updates the password for a person in the appropriate repository.
     *
     * @param person The person whose password should be updated
     * @param hashedPassword The new hashed password
     * @return True if update was successful, false otherwise
     */
    private boolean updatePasswordForPerson(AuthenticablePerson person, String hashedPassword) {
        try {
            person.setPassword(hashedPassword);

            if (person instanceof User user) {
                userRepository.addUser(user); // addUser also updates if exists
                return true;
            } else if (person instanceof Admin admin) {
                adminRepository.addAdmin(admin); // addAdmin also updates if exists
                return true;
            } else if (person instanceof DeliveryPerson deliveryPerson) {
                deliveryPersonRepository.addDeliveryPerson(deliveryPerson);
                return true;
            }

            return false;
        } catch (Exception e) {
            Logger.error("Error updating password: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================================
    // RESULT CLASSES
    // =========================================================================================

    /**
     * Result of a user identity verification operation.
     */
    public static class VerificationResult {
        private final boolean success;
        private final String message;

        public VerificationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Result of a password reset operation.
     */
    public static class ResetResult {
        private final boolean success;
        private final String message;

        public ResetResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
