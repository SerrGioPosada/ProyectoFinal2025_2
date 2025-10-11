package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Service to handle Google OAuth2 authentication for desktop applications.
 * This implementation uses a simple HTTP server instead of Jetty to avoid module conflicts.
 */
public class GoogleOAuthService {

    private static final String CLIENT_ID = "307704039867-2piv7j9num96kuai0j2e3gja7e6ud0i8.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final int LOCAL_PORT = 8888;
    private static final String REDIRECT_URI = "http://localhost:" + LOCAL_PORT;

    private static final String AUTH_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v2/userinfo";

    private final NetHttpTransport httpTransport;
    private final JsonFactory jsonFactory;

    public GoogleOAuthService() {
        this.httpTransport = new NetHttpTransport();
        this.jsonFactory = JacksonFactory.getDefaultInstance();
    }

    /**
     * Initiates the Google OAuth2 flow and returns user information.
     */
    public CompletableFuture<GoogleUserInfo> authenticate() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Crear el flujo de autorización
                AuthorizationCodeFlow flow = createFlow();

                // 2. Generar URL de autorización
                String authUrl = flow.newAuthorizationUrl()
                        .setRedirectUri(REDIRECT_URI)
                        .build();

                // 3. Abrir navegador
                openBrowser(authUrl);

                // 4. Esperar el código de autorización
                String authCode = waitForAuthorizationCode();

                // 5. Intercambiar código por token
                TokenResponse tokenResponse = flow.newTokenRequest(authCode)
                        .setRedirectUri(REDIRECT_URI)
                        .execute();

                // 6. Obtener información del usuario
                GoogleUserInfo userInfo = fetchUserInfo(tokenResponse.getAccessToken());

                return userInfo;

            } catch (Exception e) {
                throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Creates the Google Authorization Code Flow.
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
     */
    private void openBrowser(String url) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI.create(url));
        } else {
            System.out.println("Please open this URL in your browser:");
            System.out.println(url);
        }
    }

    /**
     * Starts a simple HTTP server and waits for the authorization code.
     */
    private String waitForAuthorizationCode() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(LOCAL_PORT)) {
            serverSocket.setSoTimeout(120000); // 2 minutos timeout

            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            // Leer la primera línea de la petición HTTP
            String request = reader.readLine();
            System.out.println("Request recibida: " + request); // Debug

            // Extraer el código de autorización
            String code = null;
            if (request != null && request.contains("code=")) {
                // Formato: GET /?code=XXXXX&scope=... HTTP/1.1
                int start = request.indexOf("code=") + 5;
                int end = request.indexOf("&", start);
                if (end == -1) {
                    end = request.indexOf(" ", start);
                }
                if (end == -1) {
                    end = request.length();
                }
                code = request.substring(start, end);

                // Decodificar URL si es necesario
                code = java.net.URLDecoder.decode(code, "UTF-8");
                System.out.println("Code extraído: " + code); // Debug
            }

            // Enviar respuesta al navegador
            String htmlResponse = code != null
                    ? "<html><body style='font-family: Arial; text-align: center; padding: 50px;'><h1 style='color: green;'>✓ Login Successful!</h1><p>You can close this window and return to the application.</p></body></html>"
                    : "<html><body style='font-family: Arial; text-align: center; padding: 50px;'><h1 style='color: red;'>✗ Login Failed</h1><p>No authorization code received.</p></body></html>";

            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    htmlResponse;

            OutputStream os = socket.getOutputStream();
            os.write(httpResponse.getBytes("UTF-8"));
            os.flush();
            socket.close();

            if (code == null || code.isEmpty()) {
                throw new IOException("No authorization code received");
            }

            return code;
        }
    }

    /**
     * Fetches user information from Google using the access token.
     */
    private GoogleUserInfo fetchUserInfo(String accessToken) throws IOException {
        java.net.URL url = new java.net.URL(USER_INFO_URI + "?access_token=" + accessToken);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to fetch user info: " + conn.getResponseCode());
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

        // Parsear JSON usando Gson
        JsonObject jsonObject = JsonParser.parseString(jsonResponse.toString()).getAsJsonObject();

        String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : "";
        String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
        String picture = jsonObject.has("picture") ? jsonObject.get("picture").getAsString() : "";

        return new GoogleUserInfo(email, name, picture);
    }

    /**
     * Data class to hold Google user information.
     */
    public static class GoogleUserInfo {
        private final String email;
        private final String name;
        private final String picture;

        public GoogleUserInfo(String email, String name, String picture) {
            this.email = email;
            this.name = name;
            this.picture = picture;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public String getPicture() {
            return picture;
        }

        @Override
        public String toString() {
            return "GoogleUserInfo{email='" + email + "', name='" + name + "'}";
        }
    }
}