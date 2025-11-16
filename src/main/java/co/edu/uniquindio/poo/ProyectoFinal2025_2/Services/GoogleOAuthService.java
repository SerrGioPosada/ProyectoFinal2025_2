package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Config.ConfigLoader;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.StringUtil;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Service to handle Google OAuth2 authentication for desktop applications.
 * This implementation uses a simple HTTP server to handle the OAuth callback.
 */
public class GoogleOAuthService {

    // OAuth credentials loaded from config/oauth.properties
    private static final String CLIENT_ID = ConfigLoader.getGoogleClientId();
    private static final String CLIENT_SECRET = ConfigLoader.getGoogleClientSecret();
    private static final int LOCAL_PORT = ConfigLoader.getGoogleRedirectPort();
    private static final String REDIRECT_URI = "http://localhost:" + LOCAL_PORT;

    private static final String AUTH_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v2/userinfo";

    private static final int TIMEOUT_MS = 120000;
    private static final String ENCODING = "UTF-8";

    private final NetHttpTransport httpTransport;
    private final JsonFactory jsonFactory;

    /**
     * Constructs a new GoogleOAuthService with default HTTP transport and JSON factory.
     */
    public GoogleOAuthService() {
        this.httpTransport = new NetHttpTransport();
        this.jsonFactory = JacksonFactory.getDefaultInstance();
    }

    /**
     * Initiates the Google OAuth2 flow and returns user information asynchronously.
     * This method will open the system's default browser for user authentication.
     *
     * @return A CompletableFuture containing the GoogleUserInfo if successful.
     */
    public CompletableFuture<GoogleUserInfo> authenticate() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Logger.info("Starting Google OAuth2 authentication flow");

                AuthorizationCodeFlow flow = createFlow();
                String authUrl = flow.newAuthorizationUrl()
                        .setRedirectUri(REDIRECT_URI)
                        .build();

                openBrowser(authUrl);

                String authCode = waitForAuthorizationCode();
                Logger.info("Authorization code received");

                TokenResponse tokenResponse = flow.newTokenRequest(authCode)
                        .setRedirectUri(REDIRECT_URI)
                        .execute();

                GoogleUserInfo userInfo = fetchUserInfo(tokenResponse.getAccessToken());
                Logger.info("Google authentication successful for: " + userInfo.getEmail());

                return userInfo;

            } catch (Exception e) {
                Logger.error("Google authentication failed: " + e.getMessage(), e);
                throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Creates the Google Authorization Code Flow with required scopes.
     *
     * @return The configured AuthorizationCodeFlow.
     */
    private AuthorizationCodeFlow createFlow() {
        return new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                httpTransport,
                jsonFactory,
                new GenericUrl(TOKEN_URI),
                new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
                CLIENT_ID,
                AUTH_URI
        ).setScopes(Collections.singletonList("openid email profile"))
                .build();
    }

    /**
     * Opens the system's default browser with the authorization URL.
     * If Desktop is not supported, prints the URL to console.
     *
     * @param url The authorization URL to open.
     * @throws IOException if unable to open the browser.
     */
    private void openBrowser(String url) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI.create(url));
            Logger.info("Browser opened for authentication");
        } else {
            Logger.warn("Desktop not supported. Please open this URL in your browser: " + url);
        }
    }

    /**
     * Starts a simple HTTP server and waits for the authorization code from the OAuth callback.
     * The server will timeout after 2 minutes if no response is received.
     *
     * @return The authorization code extracted from the callback.
     * @throws IOException if the server fails or no code is received.
     */
    private String waitForAuthorizationCode() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(LOCAL_PORT)) {
            serverSocket.setSoTimeout(TIMEOUT_MS);
            Logger.info("Waiting for OAuth callback on port " + LOCAL_PORT);

            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            String request = reader.readLine();
            Logger.debug("Request received: " + request);

            String code = extractAuthorizationCode(request);

            sendResponseToUser(socket, code != null);
            socket.close();

            if (StringUtil.isNullOrEmpty(code)) {
                throw new IOException("No authorization code received");
            }

            return code;
        }
    }

    /**
     * Extracts the authorization code from the HTTP request.
     *
     * @param request The HTTP request line.
     * @return The extracted authorization code, or null if not found.
     * @throws IOException if URL decoding fails.
     */
    private String extractAuthorizationCode(String request) throws IOException {
        if (request == null || !request.contains("code=")) {
            return null;
        }

        int start = request.indexOf("code=") + 5;
        int end = request.indexOf("&", start);
        if (end == -1) {
            end = request.indexOf(" ", start);
        }
        if (end == -1) {
            end = request.length();
        }

        String code = request.substring(start, end);
        code = URLDecoder.decode(code, ENCODING);
        Logger.debug("Authorization code extracted: " + code.substring(0, Math.min(10, code.length())) + "...");

        return code;
    }

    /**
     * Sends an HTML response to the user's browser indicating success or failure.
     *
     * @param socket  The socket connection to the browser.
     * @param success Whether the authentication was successful.
     * @throws IOException if unable to send the response.
     */
    private void sendResponseToUser(Socket socket, boolean success) throws IOException {
        String htmlResponse = success
                ? buildSuccessResponse()
                : buildErrorResponse();

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                htmlResponse;

        OutputStream os = socket.getOutputStream();
        os.write(httpResponse.getBytes(ENCODING));
        os.flush();
    }

    /**
     * Builds the HTML success response.
     *
     * @return HTML string for successful authentication.
     */
    private String buildSuccessResponse() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Autenticación Exitosa - UniQuindío Envíos</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
                    <style>
                        :root {
                            --primary-dark: #032d4d;
                            --primary-blue: #0A4969;
                            --primary-accent: #08344C;
                            --success-green: #10b981;
                            --success-light: #d1fae5;
                        }

                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }

                        body {
                            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                            background: linear-gradient(135deg, var(--primary-dark) 0%, var(--primary-blue) 50%, var(--primary-accent) 100%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            overflow: hidden;
                            position: relative;
                        }

                        /* Sophisticated animated background grid */
                        .grid-background {
                            position: absolute;
                            width: 100%;
                            height: 100%;
                            background-image:
                                linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
                                linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
                            background-size: 50px 50px;
                            animation: gridMove 20s linear infinite;
                            z-index: 0;
                        }

                        @keyframes gridMove {
                            0% { transform: translate(0, 0); }
                            100% { transform: translate(50px, 50px); }
                        }

                        /* Floating orbs */
                        .orbs {
                            position: absolute;
                            width: 100%;
                            height: 100%;
                            overflow: hidden;
                            z-index: 1;
                        }

                        .orb {
                            position: absolute;
                            border-radius: 50%;
                            background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.2), rgba(255,255,255,0.05));
                            filter: blur(2px);
                            animation: float 20s ease-in-out infinite;
                        }

                        .orb:nth-child(1) {
                            width: 300px;
                            height: 300px;
                            top: -150px;
                            left: -150px;
                            animation-delay: 0s;
                        }

                        .orb:nth-child(2) {
                            width: 200px;
                            height: 200px;
                            top: 50%;
                            right: -100px;
                            animation-delay: 3s;
                        }

                        .orb:nth-child(3) {
                            width: 250px;
                            height: 250px;
                            bottom: -125px;
                            left: 30%;
                            animation-delay: 6s;
                        }

                        @keyframes float {
                            0%, 100% { transform: translate(0, 0) scale(1); }
                            33% { transform: translate(30px, -30px) scale(1.1); }
                            66% { transform: translate(-20px, 20px) scale(0.9); }
                        }

                        /* Main container */
                        .container {
                            position: relative;
                            z-index: 10;
                            background: rgba(255, 255, 255, 0.98);
                            backdrop-filter: blur(10px);
                            padding: 70px 90px;
                            border-radius: 24px;
                            box-shadow:
                                0 25px 50px rgba(0, 0, 0, 0.3),
                                0 0 100px rgba(10, 73, 105, 0.2),
                                inset 0 1px 0 rgba(255, 255, 255, 0.5);
                            text-align: center;
                            max-width: 580px;
                            animation: containerEntrance 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
                            border: 1px solid rgba(255, 255, 255, 0.18);
                        }

                        @keyframes containerEntrance {
                            0% {
                                opacity: 0;
                                transform: translateY(40px) scale(0.9);
                            }
                            100% {
                                opacity: 1;
                                transform: translateY(0) scale(1);
                            }
                        }

                        /* Success icon with advanced animation */
                        .success-wrapper {
                            width: 140px;
                            height: 140px;
                            margin: 0 auto 40px;
                            position: relative;
                        }

                        .success-circle-bg {
                            position: absolute;
                            width: 100%;
                            height: 100%;
                            background: linear-gradient(135deg, var(--success-light), #a7f3d0);
                            border-radius: 50%;
                            animation: pulsate 2s ease-in-out infinite;
                        }

                        @keyframes pulsate {
                            0%, 100% { transform: scale(1); opacity: 0.7; }
                            50% { transform: scale(1.05); opacity: 1; }
                        }

                        .success-icon {
                            position: relative;
                            width: 100%;
                            height: 100%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }

                        .checkmark-svg {
                            width: 60%;
                            height: 60%;
                        }

                        .checkmark-circle {
                            stroke: var(--success-green);
                            stroke-width: 4;
                            fill: none;
                            stroke-dasharray: 280;
                            stroke-dashoffset: 280;
                            animation: drawCircle 0.6s cubic-bezier(0.65, 0, 0.45, 1) 0.2s forwards;
                        }

                        .checkmark-check {
                            stroke: var(--success-green);
                            stroke-width: 5;
                            stroke-linecap: round;
                            stroke-linejoin: round;
                            fill: none;
                            stroke-dasharray: 60;
                            stroke-dashoffset: 60;
                            animation: drawCheck 0.4s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards;
                        }

                        @keyframes drawCircle {
                            to { stroke-dashoffset: 0; }
                        }

                        @keyframes drawCheck {
                            to { stroke-dashoffset: 0; }
                        }

                        /* Typography */
                        h1 {
                            color: var(--primary-dark);
                            font-size: 38px;
                            font-weight: 800;
                            margin-bottom: 16px;
                            letter-spacing: -0.5px;
                            animation: fadeSlideUp 0.6s ease-out 0.4s both;
                        }

                        .subtitle {
                            color: #64748b;
                            font-size: 17px;
                            font-weight: 400;
                            margin-bottom: 40px;
                            line-height: 1.6;
                            animation: fadeSlideUp 0.6s ease-out 0.6s both;
                        }

                        @keyframes fadeSlideUp {
                            from {
                                opacity: 0;
                                transform: translateY(20px);
                            }
                            to {
                                opacity: 1;
                                transform: translateY(0);
                            }
                        }

                        /* Info cards */
                        .info-cards {
                            display: flex;
                            gap: 12px;
                            margin-bottom: 40px;
                            animation: fadeSlideUp 0.6s ease-out 0.8s both;
                        }

                        .info-card {
                            flex: 1;
                            background: linear-gradient(135deg, #f0fdf4, #dcfce7);
                            padding: 20px 16px;
                            border-radius: 12px;
                            border: 1px solid #bbf7d0;
                            display: flex;
                            align-items: center;
                            gap: 12px;
                        }

                        .info-card-icon {
                            width: 24px;
                            height: 24px;
                            background: var(--success-green);
                            border-radius: 50%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            color: white;
                            font-weight: 700;
                            font-size: 14px;
                            flex-shrink: 0;
                        }

                        .info-card-text {
                            color: #166534;
                            font-size: 14px;
                            font-weight: 500;
                            text-align: left;
                        }

                        /* Buttons */
                        .button-group {
                            display: flex;
                            gap: 12px;
                            animation: fadeSlideUp 0.6s ease-out 1s both;
                        }

                        .btn {
                            flex: 1;
                            padding: 16px 32px;
                            border-radius: 12px;
                            font-size: 16px;
                            font-weight: 600;
                            cursor: pointer;
                            border: none;
                            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                            font-family: 'Inter', sans-serif;
                            position: relative;
                            overflow: hidden;
                        }

                        .btn-primary {
                            background: linear-gradient(135deg, var(--primary-blue), var(--primary-dark));
                            color: white;
                            box-shadow: 0 4px 12px rgba(3, 45, 77, 0.3);
                        }

                        .btn-primary::before {
                            content: '';
                            position: absolute;
                            top: 0;
                            left: -100%;
                            width: 100%;
                            height: 100%;
                            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
                            transition: left 0.5s;
                        }

                        .btn-primary:hover::before {
                            left: 100%;
                        }

                        .btn-primary:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 8px 20px rgba(3, 45, 77, 0.4);
                        }

                        .btn-primary:active {
                            transform: translateY(0);
                        }

                        /* Auto-close timer */
                        .timer {
                            margin-top: 30px;
                            color: #94a3b8;
                            font-size: 13px;
                            animation: fadeSlideUp 0.6s ease-out 1.2s both;
                        }

                        .timer-number {
                            color: var(--primary-blue);
                            font-weight: 700;
                        }

                        /* Responsive */
                        @media (max-width: 640px) {
                            .container {
                                padding: 50px 40px;
                            }

                            h1 {
                                font-size: 28px;
                            }

                            .info-cards {
                                flex-direction: column;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="grid-background"></div>
                    <div class="orbs">
                        <div class="orb"></div>
                        <div class="orb"></div>
                        <div class="orb"></div>
                    </div>

                    <div class="container">
                        <div class="success-wrapper">
                            <div class="success-circle-bg"></div>
                            <div class="success-icon">
                                <svg class="checkmark-svg" viewBox="0 0 100 100">
                                    <circle class="checkmark-circle" cx="50" cy="50" r="45"/>
                                    <path class="checkmark-check" d="M25,52 L40,67 L75,32"/>
                                </svg>
                            </div>
                        </div>

                        <h1>¡Autenticación Exitosa!</h1>
                        <p class="subtitle">Has iniciado sesión correctamente con tu cuenta de Google.</p>

                        <div class="info-cards">
                            <div class="info-card">
                                <div class="info-card-icon">✓</div>
                                <div class="info-card-text">Sesión establecida</div>
                            </div>
                            <div class="info-card">
                                <div class="info-card-icon">✓</div>
                                <div class="info-card-text">Regresa a la app</div>
                            </div>
                        </div>

                        <div class="button-group">
                            <button class="btn btn-primary" onclick="closeWindow()">
                                Cerrar esta ventana
                            </button>
                        </div>

                        <div class="timer">
                            Cierre automático en <span class="timer-number" id="countdown">8</span> segundos
                        </div>
                    </div>

                    <script>
                        let timeLeft = 8;
                        const countdownEl = document.getElementById('countdown');

                        const timer = setInterval(() => {
                            timeLeft--;
                            if (countdownEl) {
                                countdownEl.textContent = timeLeft;
                            }

                            if (timeLeft <= 0) {
                                clearInterval(timer);
                                closeWindow();
                            }
                        }, 1000);

                        function closeWindow() {
                            window.close();
                            // Fallback for browsers that don't allow window.close()
                            setTimeout(() => {
                                document.body.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100vh;font-family:Inter,sans-serif;color:white;font-size:18px;">Puedes cerrar esta ventana manualmente</div>';
                            }, 100);
                        }
                    </script>
                </body>
                </html>
                """;
    }

    /**
     * Builds the HTML error response.
     *
     * @return HTML string for failed authentication.
     */
    private String buildErrorResponse() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Error de Autenticación - UniQuindío Envíos</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
                    <style>
                        :root {
                            --primary-dark: #032d4d;
                            --primary-blue: #0A4969;
                            --error-red: #dc3545;
                            --error-light: #fee2e2;
                        }

                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }

                        body {
                            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                            background: linear-gradient(135deg, var(--primary-dark) 0%, var(--primary-blue) 50%, #08344C 100%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            overflow: hidden;
                            position: relative;
                        }

                        /* Grid background */
                        .grid-background {
                            position: absolute;
                            width: 100%;
                            height: 100%;
                            background-image:
                                linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
                                linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
                            background-size: 50px 50px;
                            animation: gridMove 20s linear infinite;
                            z-index: 0;
                        }

                        @keyframes gridMove {
                            0% { transform: translate(0, 0); }
                            100% { transform: translate(50px, 50px); }
                        }

                        /* Floating orbs */
                        .orbs {
                            position: absolute;
                            width: 100%;
                            height: 100%;
                            overflow: hidden;
                            z-index: 1;
                        }

                        .orb {
                            position: absolute;
                            border-radius: 50%;
                            background: radial-gradient(circle at 30% 30%, rgba(220, 53, 69, 0.3), rgba(220, 53, 69, 0.05));
                            filter: blur(2px);
                            animation: float 20s ease-in-out infinite;
                        }

                        .orb:nth-child(1) {
                            width: 300px;
                            height: 300px;
                            top: -150px;
                            left: -150px;
                            animation-delay: 0s;
                        }

                        .orb:nth-child(2) {
                            width: 200px;
                            height: 200px;
                            top: 50%;
                            right: -100px;
                            animation-delay: 3s;
                        }

                        .orb:nth-child(3) {
                            width: 250px;
                            height: 250px;
                            bottom: -125px;
                            left: 30%;
                            animation-delay: 6s;
                        }

                        @keyframes float {
                            0%, 100% { transform: translate(0, 0) scale(1); }
                            33% { transform: translate(30px, -30px) scale(1.1); }
                            66% { transform: translate(-20px, 20px) scale(0.9); }
                        }

                        .container {
                            position: relative;
                            z-index: 10;
                            background: rgba(255, 255, 255, 0.98);
                            backdrop-filter: blur(10px);
                            padding: 70px 90px;
                            border-radius: 24px;
                            box-shadow:
                                0 25px 50px rgba(0, 0, 0, 0.3),
                                0 0 100px rgba(220, 53, 69, 0.2),
                                inset 0 1px 0 rgba(255, 255, 255, 0.5);
                            text-align: center;
                            max-width: 580px;
                            animation: containerEntrance 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
                            border: 1px solid rgba(255, 255, 255, 0.18);
                        }

                        @keyframes containerEntrance {
                            0% {
                                opacity: 0;
                                transform: translateY(40px) scale(0.9);
                            }
                            100% {
                                opacity: 1;
                                transform: translateY(0) scale(1);
                            }
                        }

                        /* Error icon */
                        .error-wrapper {
                            width: 140px;
                            height: 140px;
                            margin: 0 auto 40px;
                            position: relative;
                        }

                        .error-circle-bg {
                            position: absolute;
                            width: 100%;
                            height: 100%;
                            background: linear-gradient(135deg, var(--error-light), #fecaca);
                            border-radius: 50%;
                            animation: pulsate 2s ease-in-out infinite;
                        }

                        @keyframes pulsate {
                            0%, 100% { transform: scale(1); opacity: 0.7; }
                            50% { transform: scale(1.05); opacity: 1; }
                        }

                        .error-icon {
                            position: relative;
                            width: 100%;
                            height: 100%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }

                        .error-svg {
                            width: 60%;
                            height: 60%;
                        }

                        .error-circle {
                            stroke: var(--error-red);
                            stroke-width: 4;
                            fill: none;
                            stroke-dasharray: 280;
                            stroke-dashoffset: 280;
                            animation: drawCircle 0.6s cubic-bezier(0.65, 0, 0.45, 1) 0.2s forwards;
                        }

                        .error-cross {
                            stroke: var(--error-red);
                            stroke-width: 5;
                            stroke-linecap: round;
                            fill: none;
                            stroke-dasharray: 60;
                            stroke-dashoffset: 60;
                            animation: drawCheck 0.4s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards;
                        }

                        @keyframes drawCircle {
                            to { stroke-dashoffset: 0; }
                        }

                        @keyframes drawCheck {
                            to { stroke-dashoffset: 0; }
                        }

                        /* Typography */
                        h1 {
                            color: var(--error-red);
                            font-size: 38px;
                            font-weight: 800;
                            margin-bottom: 16px;
                            letter-spacing: -0.5px;
                            animation: fadeSlideUp 0.6s ease-out 0.4s both;
                        }

                        .subtitle {
                            color: #64748b;
                            font-size: 17px;
                            font-weight: 400;
                            margin-bottom: 40px;
                            line-height: 1.6;
                            animation: fadeSlideUp 0.6s ease-out 0.6s both;
                        }

                        @keyframes fadeSlideUp {
                            from {
                                opacity: 0;
                                transform: translateY(20px);
                            }
                            to {
                                opacity: 1;
                                transform: translateY(0);
                            }
                        }

                        /* Info cards */
                        .info-cards {
                            display: flex;
                            flex-direction: column;
                            gap: 12px;
                            margin-bottom: 40px;
                            animation: fadeSlideUp 0.6s ease-out 0.8s both;
                        }

                        .info-card {
                            background: linear-gradient(135deg, #fef2f2, #fee2e2);
                            padding: 16px 20px;
                            border-radius: 12px;
                            border: 1px solid #fecaca;
                            display: flex;
                            align-items: center;
                            gap: 12px;
                            text-align: left;
                        }

                        .info-card-icon {
                            width: 24px;
                            height: 24px;
                            background: var(--error-red);
                            border-radius: 50%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            color: white;
                            font-weight: 700;
                            font-size: 14px;
                            flex-shrink: 0;
                        }

                        .info-card-text {
                            color: #991b1b;
                            font-size: 14px;
                            font-weight: 500;
                        }

                        /* Buttons */
                        .button-group {
                            animation: fadeSlideUp 0.6s ease-out 1s both;
                        }

                        .btn {
                            width: 100%;
                            padding: 16px 32px;
                            border-radius: 12px;
                            font-size: 16px;
                            font-weight: 600;
                            cursor: pointer;
                            border: none;
                            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                            font-family: 'Inter', sans-serif;
                            position: relative;
                            overflow: hidden;
                            background: linear-gradient(135deg, var(--primary-blue), var(--primary-dark));
                            color: white;
                            box-shadow: 0 4px 12px rgba(3, 45, 77, 0.3);
                        }

                        .btn::before {
                            content: '';
                            position: absolute;
                            top: 0;
                            left: -100%;
                            width: 100%;
                            height: 100%;
                            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
                            transition: left 0.5s;
                        }

                        .btn:hover::before {
                            left: 100%;
                        }

                        .btn:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 8px 20px rgba(3, 45, 77, 0.4);
                        }

                        .btn:active {
                            transform: translateY(0);
                        }

                        /* Timer */
                        .timer {
                            margin-top: 30px;
                            color: #94a3b8;
                            font-size: 13px;
                            animation: fadeSlideUp 0.6s ease-out 1.2s both;
                        }

                        .timer-number {
                            color: var(--error-red);
                            font-weight: 700;
                        }

                        /* Responsive */
                        @media (max-width: 640px) {
                            .container {
                                padding: 50px 40px;
                            }

                            h1 {
                                font-size: 28px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="grid-background"></div>
                    <div class="orbs">
                        <div class="orb"></div>
                        <div class="orb"></div>
                        <div class="orb"></div>
                    </div>

                    <div class="container">
                        <div class="error-wrapper">
                            <div class="error-circle-bg"></div>
                            <div class="error-icon">
                                <svg class="error-svg" viewBox="0 0 100 100">
                                    <circle class="error-circle" cx="50" cy="50" r="45"/>
                                    <line class="error-cross" x1="30" y1="30" x2="70" y2="70"/>
                                    <line class="error-cross" x1="70" y1="30" x2="30" y2="70"/>
                                </svg>
                            </div>
                        </div>

                        <h1>Error de Autenticación</h1>
                        <p class="subtitle">No se pudo completar el proceso de inicio de sesión con Google.</p>

                        <div class="info-cards">
                            <div class="info-card">
                                <div class="info-card-icon">✗</div>
                                <div class="info-card-text">No se recibió el código de autorización</div>
                            </div>
                            <div class="info-card">
                                <div class="info-card-icon">!</div>
                                <div class="info-card-text">Intenta nuevamente desde la aplicación</div>
                            </div>
                            <div class="info-card">
                                <div class="info-card-icon">?</div>
                                <div class="info-card-text">Verifica tu conexión a internet</div>
                            </div>
                        </div>

                        <div class="button-group">
                            <button class="btn" onclick="closeWindow()">
                                Cerrar esta ventana
                            </button>
                        </div>

                        <div class="timer">
                            Cierre automático en <span class="timer-number" id="countdown">15</span> segundos
                        </div>
                    </div>

                    <script>
                        let timeLeft = 15;
                        const countdownEl = document.getElementById('countdown');

                        const timer = setInterval(() => {
                            timeLeft--;
                            if (countdownEl) {
                                countdownEl.textContent = timeLeft;
                            }

                            if (timeLeft <= 0) {
                                clearInterval(timer);
                                closeWindow();
                            }
                        }, 1000);

                        function closeWindow() {
                            window.close();
                            // Fallback for browsers that don't allow window.close()
                            setTimeout(() => {
                                document.body.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100vh;font-family:Inter,sans-serif;color:white;font-size:18px;">Puedes cerrar esta ventana manualmente</div>';
                            }, 100);
                        }
                    </script>
                </body>
                </html>
                """;
    }

    /**
     * Fetches user information from Google using the access token.
     *
     * @param accessToken The OAuth2 access token.
     * @return GoogleUserInfo containing email, name, and picture URL.
     * @throws IOException if the HTTP request fails.
     */
    private GoogleUserInfo fetchUserInfo(String accessToken) throws IOException {
        java.net.URL url = new java.net.URL(USER_INFO_URI + "?access_token=" + accessToken);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to fetch user info: HTTP " + conn.getResponseCode());
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );
        StringBuilder jsonResponse = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonResponse.append(line);
        }
        reader.close();

        JsonObject jsonObject = JsonParser.parseString(jsonResponse.toString()).getAsJsonObject();

        String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : "";
        String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
        String picture = jsonObject.has("picture") ? jsonObject.get("picture").getAsString() : "";

        return new GoogleUserInfo(email, name, picture);
    }

    /**
     * Data class to hold Google user information retrieved from OAuth.
     */
    public static class GoogleUserInfo {
        private final String email;
        private final String name;
        private final String picture;

        /**
         * Constructs a GoogleUserInfo with the provided details.
         *
         * @param email   The user's email address.
         * @param name    The user's full name.
         * @param picture The URL of the user's profile picture.
         */
        public GoogleUserInfo(String email, String name, String picture) {
            this.email = email;
            this.name = name;
            this.picture = picture;
        }

        /**
         * Gets the user's email address.
         *
         * @return The email address.
         */
        public String getEmail() {
            return email;
        }

        /**
         * Gets the user's full name.
         *
         * @return The full name.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the URL of the user's profile picture.
         *
         * @return The picture URL.
         */
        public String getPicture() {
            return picture;
        }

        @Override
        public String toString() {
            return "GoogleUserInfo{email='" + email + "', name='" + name + "'}";
        }
    }
}
