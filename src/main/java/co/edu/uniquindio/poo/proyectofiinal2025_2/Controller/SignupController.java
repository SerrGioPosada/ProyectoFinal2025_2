package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.regex.Pattern;

/**
 * Controller for the Signup view (Signup.fxml).
 * Handles user registration with real-time validation on focus lost.
 */
public class SignupController {

    @FXML private TextField txtName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtConfirmPasswordVisible;
    @FXML private Button btnRegister;
    @FXML private Label lblError;
    @FXML private Label lblNameError;
    @FXML private Label lblLastNameError;
    @FXML private Label lblEmailError;
    @FXML private Label lblPhoneError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblConfirmPasswordError;
    @FXML private Label googleSignupLabel;
    @FXML private Label lblAlreadyRegistered;
    @FXML private ImageView imgTogglePassword;
    @FXML private ImageView imgToggleConfirmPassword;

    private final UserService userService = UserService.getInstance();
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private IndexController indexController;

    // Regex patterns para validaciones
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    /**
     * Permite al IndexController pasarse a sí mismo al SignupController
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML
    public void initialize() {
        setupEventHandlers();
        setupValidations();
        setupPasswordToggles();
        setupWindowCloseHandler();
    }

    /**
     * Configura el handler para cuando se cierra la ventana
     */
    private void setupWindowCloseHandler() {
        Platform.runLater(() -> {
            if (btnRegister != null && btnRegister.getScene() != null) {
                Stage stage = (Stage) btnRegister.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    System.out.println("Registro cancelado por el usuario");
                });
            }
        });
    }

    /**
     * Configura los event handlers de botones y labels
     */
    private void setupEventHandlers() {
        if (btnRegister != null) {
            btnRegister.setOnAction(event -> handleRegister());
        }
        if (googleSignupLabel != null) {
            googleSignupLabel.setOnMouseClicked(event -> handleGoogleSignup());
        }
        if (lblAlreadyRegistered != null) {
            lblAlreadyRegistered.setOnMouseClicked(event -> handleAlreadyRegistered());
        }
    }

    /**
     * Configura las validaciones en tiempo real (al perder el focus)
     */
    private void setupValidations() {
        if (txtName != null && lblNameError != null) {
            txtName.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    validateName();
                }
            });
        }

        if (txtLastName != null && lblLastNameError != null) {
            txtLastName.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    validateLastName();
                }
            });
        }

        if (txtEmail != null && lblEmailError != null) {
            txtEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    validateEmail();
                }
            });
        }

        if (txtPhone != null && lblPhoneError != null) {
            txtPhone.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    validatePhone();
                }
            });
        }

        if (txtPassword != null && lblPasswordError != null) {
            txtPassword.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    validatePassword();
                }
            });
        }

        if (txtPasswordVisible != null && lblPasswordError != null) {
            txtPasswordVisible.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    validatePassword();
                }
            });
        }

        if (txtConfirmPassword != null && lblConfirmPasswordError != null) {
            txtConfirmPassword.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    validateConfirmPassword();
                }
            });
        }

        if (txtConfirmPasswordVisible != null && lblConfirmPasswordError != null) {
            txtConfirmPasswordVisible.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    validateConfirmPassword();
                }
            });
        }
    }

    /**
     * Configura los toggles de contraseña con imágenes
     */
    private void setupPasswordToggles() {
        if (txtPassword != null && txtPasswordVisible != null) {
            txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());
        }
        if (txtConfirmPassword != null && txtConfirmPasswordVisible != null) {
            txtConfirmPassword.textProperty().bindBidirectional(txtConfirmPasswordVisible.textProperty());
        }

        updatePasswordToggleIcon();
        updateConfirmPasswordToggleIcon();

        if (imgTogglePassword != null) {
            imgTogglePassword.setOnMouseClicked(event -> togglePassword());
        }

        if (imgToggleConfirmPassword != null) {
            imgToggleConfirmPassword.setOnMouseClicked(event -> toggleConfirmPassword());
        }
    }

    private void updatePasswordToggleIcon() {
        if (imgTogglePassword != null) {
            try {
                String iconPath = isPasswordVisible
                        ? "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-open.png"
                        : "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-closed.png";

                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                imgTogglePassword.setImage(icon);
            } catch (Exception e) {
                System.err.println("Error cargando icono del ojo: " + e.getMessage());
            }
        }
    }

    private void updateConfirmPasswordToggleIcon() {
        if (imgToggleConfirmPassword != null) {
            try {
                String iconPath = isConfirmPasswordVisible
                        ? "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-open.png"
                        : "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/eye-closed.png";

                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                imgToggleConfirmPassword.setImage(icon);
            } catch (Exception e) {
                System.err.println("Error cargando icono del ojo: " + e.getMessage());
            }
        }
    }

    private void togglePassword() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
        } else {
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
        }

        updatePasswordToggleIcon();
    }

    private void toggleConfirmPassword() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            txtConfirmPasswordVisible.setVisible(true);
            txtConfirmPasswordVisible.setManaged(true);
            txtConfirmPassword.setVisible(false);
            txtConfirmPassword.setManaged(false);
        } else {
            txtConfirmPassword.setVisible(true);
            txtConfirmPassword.setManaged(true);
            txtConfirmPasswordVisible.setVisible(false);
            txtConfirmPasswordVisible.setManaged(false);
        }

        updateConfirmPasswordToggleIcon();
    }

    private boolean validateName() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            showFieldError(lblNameError, "El nombre es obligatorio");
            return false;
        }
        if (name.length() < 2) {
            showFieldError(lblNameError, "El nombre debe tener al menos 2 caracteres");
            return false;
        }
        hideFieldError(lblNameError);
        return true;
    }

    private boolean validateLastName() {
        String lastName = txtLastName.getText().trim();
        if (lastName.isEmpty()) {
            showFieldError(lblLastNameError, "El apellido es obligatorio");
            return false;
        }
        if (lastName.length() < 2) {
            showFieldError(lblLastNameError, "El apellido debe tener al menos 2 caracteres");
            return false;
        }
        hideFieldError(lblLastNameError);
        return true;
    }

    private boolean validateEmail() {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            showFieldError(lblEmailError, "El correo electrónico es obligatorio");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showFieldError(lblEmailError, "Formato de correo inválido (ejemplo@dominio.com)");
            return false;
        }
        hideFieldError(lblEmailError);
        return true;
    }

    private boolean validatePhone() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            showFieldError(lblPhoneError, "El teléfono es obligatorio");
            return false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            showFieldError(lblPhoneError, "El teléfono debe tener exactamente 10 dígitos");
            return false;
        }
        hideFieldError(lblPhoneError);
        return true;
    }

    private boolean validatePassword() {
        String password = isPasswordVisible ? txtPasswordVisible.getText() : txtPassword.getText();
        if (password.isEmpty()) {
            showFieldError(lblPasswordError, "La contraseña es obligatoria");
            return false;
        }
        if (password.length() < 6) {
            showFieldError(lblPasswordError, "La contraseña debe tener al menos 6 caracteres");
            return false;
        }
        hideFieldError(lblPasswordError);
        return true;
    }

    private boolean validateConfirmPassword() {
        String password = isPasswordVisible ? txtPasswordVisible.getText() : txtPassword.getText();
        String confirmPassword = isConfirmPasswordVisible ? txtConfirmPasswordVisible.getText() : txtConfirmPassword.getText();

        if (confirmPassword.isEmpty()) {
            showFieldError(lblConfirmPasswordError, "Debes confirmar tu contraseña");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showFieldError(lblConfirmPasswordError, "Las contraseñas no coinciden");
            return false;
        }
        hideFieldError(lblConfirmPasswordError);
        return true;
    }

    private void showFieldError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

            autoHideLabel(errorLabel, 5000);
        }
    }

    private void hideFieldError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void handleRegister() {
        boolean isNameValid = validateName();
        boolean isLastNameValid = validateLastName();
        boolean isEmailValid = validateEmail();
        boolean isPhoneValid = validatePhone();
        boolean isPasswordValid = validatePassword();
        boolean isConfirmPasswordValid = validateConfirmPassword();

        if (!isNameValid || !isLastNameValid || !isEmailValid ||
                !isPhoneValid || !isPasswordValid || !isConfirmPasswordValid) {
            showError("Por favor, corrige los errores en el formulario");
            return;
        }

        PersonCreationData data = new PersonCreationData();
        data.setId(java.util.UUID.randomUUID().toString()); // ✅ GENERA EL ID AQUÍ
        data.setName(txtName.getText().trim());
        data.setLastName(txtLastName.getText().trim());
        data.setEmail(txtEmail.getText().trim());
        data.setPhone(txtPhone.getText().trim());
        data.setPassword(txtPassword.getText());

        boolean success = userService.registerUser(data);

        if (success) {
            showSuccess("¡Registro exitoso! Ahora puedes iniciar sesión");
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::closeWindow);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Este correo electrónico ya está registrado");
        }
    }

    private void handleGoogleSignup() {
        showError("Registro con Google - Funcionalidad próximamente");
    }

    private void handleAlreadyRegistered() {
        System.out.println("Usuario decidió iniciar sesión en lugar de registrarse");
        closeWindow();

        if (indexController != null) {
            Platform.runLater(() -> indexController.loadView("Login.fxml"));
        }
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
            lblError.setManaged(true);
            lblError.setStyle("-fx-text-fill: #ff6b6b;");

            autoHideLabel(lblError, 5000);
        }
    }

    private void showSuccess(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
            lblError.setManaged(true);
            lblError.setStyle("-fx-text-fill: #51cf66;");
        }
    }

    private void autoHideLabel(Label label, long delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                Platform.runLater(() -> {
                    if (label != null) {
                        label.setVisible(false);
                        label.setManaged(false);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void closeWindow() {
        if (btnRegister != null && btnRegister.getScene() != null) {
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.close();
        }
    }
}