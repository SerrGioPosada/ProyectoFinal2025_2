package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
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

    private static final String CLIENT_ID = "307704039867-2piv7j9num96kuai0j2e3gja7e6ud0i8.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final int LOCAL_PORT = 8888;
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
            Logger.warn("Desktop not supported. Please open this URL in your browser:");
            System.out.println(url);
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

            if (code == null || code.isEmpty()) {
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
        return "<html><body style='font-family: Arial; text-align: center; padding: 50px;'>" +
                "<h1 style='color: green;'>Login Successful!</h1>" +
                "<p>You can close this window and return to the application.</p>" +
                "</body></html>";
    }

    /**
     * Builds the HTML error response.
     *
     * @return HTML string for failed authentication.
     */
    private String buildErrorResponse() {
        return "<html><body style='font-family: Arial; text-align: center; padding: 50px;'>" +
                "<h1 style='color: red;'>Login Failed</h1>" +
                "<p>No authorization code received.</p>" +
                "</body></html>";
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