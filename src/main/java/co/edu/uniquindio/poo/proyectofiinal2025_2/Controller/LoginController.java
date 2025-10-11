package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Factory.PersonFactory;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.GoogleOAuthService;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Controller for the Login view (Login.fxml).
 * Handles user input for email and password, and uses the central
 * AuthenticationService to perform the login action. Also supports Google Sign-In.
 */
public class LoginController {

    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private Button btnLoginPane;
    @FXML
    private Label lblError;
    @FXML
    private Label googleLoginLabel;
    @FXML
    private Label lblForgotPassword;
    @FXML
    private Label lblEmailFloat;
    @FXML
    private Label lblPasswordFloat;
    @FXML
    private ImageView imgTogglePassword;
    @FXML
    private CheckBox chkKeepSignedIn;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final PersonFactory personFactory = new PersonFactory();
    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService();

    private IndexController indexController;
    private boolean isPasswordVisible = false;
    private boolean isTogglingPassword = false;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML
    public void initialize() {
        setupEventHandlers();
        setupFloatingLabels();
        setupPasswordToggle();
    }

    /**
     * Configura los event handlers de los botones y labels
     */
    private void setupEventHandlers() {
        if (btnLoginPane != null) {
            btnLoginPane.setOnAction(event -> handleTraditionalLogin());
        }
        if (googleLoginLabel != null) {
            googleLoginLabel.setOnMouseClicked(event -> handleGoogleLogin());
        }
        if (lblForgotPassword != null) {
            lblForgotPassword.setOnMouseClicked(event -> handleForgotPassword());
        }
    }

    /**
     * Configura las animaciones de floating labels
     */
    private void setupFloatingLabels() {
        // Email floating label
        if (txtEmail != null && lblEmailFloat != null) {
            lblEmailFloat.setOpacity(0);
            lblEmailFloat.setTranslateY(35);

            txtEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    animateFloatingLabel(lblEmailFloat, txtEmail, true);
                } else if (txtEmail.getText().isEmpty()) {
                    animateFloatingLabel(lblEmailFloat, txtEmail, false);
                }
            });

            txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.isEmpty() && lblEmailFloat.getOpacity() < 0.5) {
                    animateFloatingLabel(lblEmailFloat, txtEmail, true);
                }
            });
        }

        // Password floating label
        if (txtPassword != null && txtPasswordVisible != null && lblPasswordFloat != null) {
            lblPasswordFloat.setOpacity(0);
            lblPasswordFloat.setTranslateY(35);

            // Para PasswordField
            txtPassword.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (isTogglingPassword) return;

                if (newVal) {
                    animateFloatingLabel(lblPasswordFloat, txtPassword, true);
                } else if (txtPassword.getText().isEmpty()) {
                    animateFloatingLabel(lblPasswordFloat, txtPassword, false);
                }
            });

            txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
                if (isTogglingPassword) return;

                if (!newVal.isEmpty() && lblPasswordFloat.getOpacity() < 0.5) {
                    animateFloatingLabel(lblPasswordFloat, txtPassword, true);
                }
            });

            // Para TextField visible
            txtPasswordVisible.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (isTogglingPassword) return;

                if (newVal) {
                    animateFloatingLabel(lblPasswordFloat, txtPasswordVisible, true);
                } else if (txtPasswordVisible.getText().isEmpty()) {
                    animateFloatingLabel(lblPasswordFloat, txtPasswordVisible, false);
                }
            });

            txtPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
                if (isTogglingPassword) return;

                if (!newVal.isEmpty() && lblPasswordFloat.getOpacity() < 0.5) {
                    animateFloatingLabel(lblPasswordFloat, txtPasswordVisible, true);
                }
            });
        }
    }

    /**
     * Anima el floating label hacia arriba o abajo con el mismo tamaño de texto
     */
    private void animateFloatingLabel(Label label, Control field, boolean moveUp) {
        // Detener cualquier animación previa estableciendo la posición inicial
        label.setTranslateY(moveUp ? 35 : 0);

        TranslateTransition translate = new TranslateTransition(Duration.millis(200), label);
        FadeTransition fade = new FadeTransition(Duration.millis(200), label);

        if (moveUp) {
            // Configurar valores iniciales y finales
            translate.setFromY(35);
            translate.setToY(0);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            // Ocultar el prompt text ANTES de animar
            if (field instanceof TextField) {
                ((TextField) field).setPromptText("");
            } else if (field instanceof PasswordField) {
                ((PasswordField) field).setPromptText("");
            }

            if (!label.getStyleClass().contains("floating-label-top-active")) {
                label.getStyleClass().add("floating-label-top-active");
            }
        } else {
            // Configurar valores iniciales y finales
            translate.setFromY(0);
            translate.setToY(35);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            // Restaurar el prompt text DESPUÉS de que termine la animación
            fade.setOnFinished(e -> {
                if (field == txtEmail) {
                    txtEmail.setPromptText("CORREO ELECTRÓNICO");
                } else if (field == txtPassword) {
                    txtPassword.setPromptText("CONTRASEÑA");
                } else if (field == txtPasswordVisible) {
                    txtPasswordVisible.setPromptText("CONTRASEÑA");
                }
            });

            label.getStyleClass().remove("floating-label-top-active");
        }

        translate.play();
        fade.play();
    }

    /**
     * Configura el toggle de mostrar/ocultar contraseña
     */
    private void setupPasswordToggle() {
        if (imgTogglePassword != null) {
            updatePasswordToggleIcon();

            imgTogglePassword.setOnMouseClicked(event -> togglePasswordVisibility());

            if (txtPassword != null && txtPasswordVisible != null) {
                txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());
            }
        }
    }

    /**
     * Actualiza el icono del toggle de contraseña
     */
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

    /**
     * Alterna la visibilidad de la contraseña
     */
    private void togglePasswordVisibility() {
        isTogglingPassword = true;

        boolean labelIsUp = lblPasswordFloat.getOpacity() > 0.5;
        boolean hasText = !txtPassword.getText().isEmpty();

        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Cambiar a TextField visible
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);

            // MANTENER el label arriba si está arriba o si hay texto
            if (labelIsUp || hasText) {
                // Forzar el label a estar arriba sin animación
                lblPasswordFloat.setOpacity(1.0);
                lblPasswordFloat.setTranslateY(0);
                if (!lblPasswordFloat.getStyleClass().contains("floating-label-top-active")) {
                    lblPasswordFloat.getStyleClass().add("floating-label-top-active");
                }
                txtPasswordVisible.setPromptText(""); // Sin placeholder
            } else {
                txtPasswordVisible.setPromptText("CONTRASEÑA"); // Con placeholder
            }

            // NO dar focus automáticamente para evitar el borde azul
            Platform.runLater(() -> {
                isTogglingPassword = false;
            });
        } else {
            // Cambiar a PasswordField
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);

            // MANTENER el label arriba si está arriba o si hay texto
            if (labelIsUp || hasText) {
                // Forzar el label a estar arriba sin animación
                lblPasswordFloat.setOpacity(1.0);
                lblPasswordFloat.setTranslateY(0);
                if (!lblPasswordFloat.getStyleClass().contains("floating-label-top-active")) {
                    lblPasswordFloat.getStyleClass().add("floating-label-top-active");
                }
                txtPassword.setPromptText(""); // Sin placeholder
            } else {
                txtPassword.setPromptText("CONTRASEÑA"); // Con placeholder
            }

            // NO dar focus automáticamente para evitar el borde azul
            Platform.runLater(() -> {
                isTogglingPassword = false;
            });
        }

        updatePasswordToggleIcon();
    }

    /**
     * Handles the traditional login button click event.
     */
    private void handleTraditionalLogin() {
        System.out.println("=== Botón de login presionado ===");

        String email = txtEmail.getText();
        String password = isPasswordVisible ? txtPasswordVisible.getText() : txtPassword.getText();

        System.out.println("Email: " + email);
        System.out.println("Password length: " + password.length());

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Campos vacíos detectados");
            showError("El correo y la contraseña no pueden estar vacíos.");
            return;
        }

        System.out.println("Intentando login...");
        boolean loginSuccess = authService.login(email, password);
        System.out.println("Resultado del login: " + loginSuccess);

        if (loginSuccess) {
            System.out.println("Login exitoso, llamando a indexController.onLoginSuccess()");
            if (indexController != null) {
                System.out.println("IndexController no es null");
                indexController.onLoginSuccess();
            } else {
                System.out.println("ERROR: IndexController es NULL!");
            }
        } else {
            System.out.println("Login fallido");
            showError("Correo o contraseña inválidos. Por favor, inténtalo de nuevo.");
        }
    }

    /**
     * Handles Google login using OAuth2 with native implementation.
     */
    private void handleGoogleLogin() {
        if (googleLoginLabel != null) {
            googleLoginLabel.setDisable(true);
        }

        showError("Abriendo navegador para iniciar sesión con Google...");

        googleOAuthService.authenticate()
                .thenAccept(userInfo -> {
                    Platform.runLater(() -> {
                        processExternalUser(userInfo.getName(), userInfo.getEmail());
                        if (googleLoginLabel != null) {
                            googleLoginLabel.setDisable(false);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showError("Error al iniciar sesión con Google: " + ex.getMessage());
                        if (googleLoginLabel != null) {
                            googleLoginLabel.setDisable(false);
                        }
                    });
                    ex.printStackTrace();
                    return null;
                });
    }

    /**
     * Maneja el clic en "Olvidaste tu contraseña"
     */
    private void handleForgotPassword() {
        if (indexController != null) {
            // indexController.showSignupView();
        }
        System.out.println("Redirigiendo a signup/recuperación de contraseña...");
    }

    /**
     * Finds an existing user by email or creates a new one, then logs them in.
     */
    private void processExternalUser(String name, String email) {
        userRepository.findByEmail(email).ifPresentOrElse(
                existingUser -> authService.setAuthenticatedUser(existingUser),
                () -> {
                    PersonCreationData newData = new PersonCreationData.Builder()
                            .withName(name)
                            .withEmail(email)
                            .withPassword("oauth_google_user_" + System.currentTimeMillis())
                            .build();
                    authService.setAuthenticatedUser(personFactory.createPerson(PersonType.USER, newData));
                }
        );

        if (indexController != null) {
            indexController.onLoginSuccess();
        }
    }

    /**
     * Displays an error message in the UI.
     */
    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
            lblError.setManaged(true);

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(() -> {
                        lblError.setVisible(false);
                        lblError.setManaged(false);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
