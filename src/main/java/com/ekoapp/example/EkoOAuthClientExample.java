package com.ekoapp.example;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ekoapp.auth.EkoOAuthClient;
import com.ekoapp.auth.EkoOAuthToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class EkoOAuthClientExample {

    static final String CLIENT_ID = "YOUR_CLIENT_ID";
    static final String CLIENT_SECRET = "YOUR_CLIENT_SECRET";
    static final String REDIRECT_URI = "http://localhost:12345/cb";
    static final String EKO_URI = "https://example.ekoapp.com";


    public static void main (String[] args) throws Exception {
        // Host a server on port 12345
        HttpServer server = HttpServer.create(new InetSocketAddress(12345), 0);
        server.createContext("/app", new AppHandler());
        server.setExecutor(null);
        server.start();
    }

    static class AppHandler implements HttpHandler {
        String cachedState;

        @Override
        public void handle(HttpExchange t) throws IOException {

            try {
                // Extract params from url query,
                // these code will be vary according to your framework
                Map<String, String> params = parseQueryString(t.getRequestURI().getQuery());
                String error = params.getOrDefault("error", "");
                String code = params.getOrDefault("code", "");
                String state = params.getOrDefault("state", "");

                // Check for error
                if(!error.isEmpty()){
                    throw new Exception("Error: " + error);
                }

                // 1. Setup EkoOAuthClient, these values must be matched with pre-registered value on Eko db
                EkoOAuthClient authClient = new EkoOAuthClient();
                authClient.setClientId(CLIENT_ID);
                authClient.setClientSecret(CLIENT_SECRET);
                authClient.setRedirectUri(REDIRECT_URI);
                authClient.setEkoUri(EKO_URI);

                if (code.isEmpty()) {
                    // 2. If the code is empty, redirect to the authentication endpoint

                    // 2.1 Create a state and save it into the session. In this example, we will just
                    // save it in a variable for demonstration purpose, do not follow this.
                    this.cachedState = authClient.createState();

                    // 2.2 Create an authentication endpoint url
                    String authEndpointUrl = authClient.createAuthenticateUrl(cachedState);

                    // 2.3 Redirect to the authentication endpoint,
                    // these code will be vary according to your framework
                    Headers responseHeaders = t.getResponseHeaders();
                    responseHeaders.set("Location", authEndpointUrl);
                    t.sendResponseHeaders(302,0);
                }
                else {
                    // 3. Retrieve token and get user info

                    // 3.1 Validate the state
                    authClient.validateSate(this.cachedState, state);

                    // 3.2 Get user info
                    JsonObject userInfo = authClient.requestUserInfoByCode(code);
                    printUserInfo(userInfo);

                    // 3.3 Do the application logic,
                    // in this case, simply reply first name and last name of the user
                    String response = userInfo.get("firstname").getAsString() + " " +
                            userInfo.get("lastname").getAsString();


                    // 3.4 Respond,
                    // these code will be vary according to your framework
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }

            }
            catch (Exception e) {
                e.printStackTrace();

                String response = e.getMessage();
                t.sendResponseHeaders(500, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

        }
    }

    private static void printToken(EkoOAuthToken token) {
        pln("# Token #");
        pln(" - access_token: " + token.getAccessToken());
        pln(" - refresh_token: " + token.getRefreshToken());
        pln(" - token_type: " + token.getTokenType());
        pln(" - expires_in: " + token.getExpiresIn());
        pln(" - scope: " + token.getScopes());
        pln(" - id_token: " + token.getRawIdToken());
    }

    private static void printIdToken(DecodedJWT idToken) {
        pln("# Id Token #");
        pln(" - firstname: " + idToken.getClaim("firstname").asString());
        pln(" - lastname: " + idToken.getClaim("lastname").asString());
        pln(" - email: " + idToken.getClaim("email").asString());
        pln(" - position: " + idToken.getClaim("position").asString());
        pln(" - iat (issued at): " + idToken.getIssuedAt());
        pln(" - exp (expires at): " + idToken.getExpiresAt());
        pln(" - aud (audience): " + idToken.getAudience());
        pln(" - iss (issuer): " + idToken.getIssuer());
    }

    private static void printUserInfo(JsonObject userInfo) {
        Gson gson = new GsonBuilder().create();
        pln("# User Info #");
        pln(" - user_id: " + userInfo.get("_id").getAsString());
        pln(" - firstname: " + userInfo.get("firstname").getAsString());
        pln(" - lastname: " + userInfo.get("lastname").getAsString());
        pln(" - email: " + userInfo.get("email").getAsString());
        pln(" - network_id: " + userInfo.get("nid").getAsString());
        pln(" - position: " + userInfo.get("position").getAsString());
        pln(" - status: " + userInfo.get("status").getAsString());
        pln(" - extras: " + gson.toJson(userInfo.get("extras").getAsJsonObject()));
    }

    // From : https://stackoverflow.com/questions/11640025
    // Answered by : Oliv https://stackoverflow.com/users/952135/oliv
    private static Map<String, String> parseQueryString(String qs) {
        Map<String, String> result = new HashMap<>();
        if (qs == null)
            return result;

        int last = 0, next, l = qs.length();
        while (last < l) {
            next = qs.indexOf('&', last);
            if (next == -1)
                next = l;

            if (next > last) {
                int eqPos = qs.indexOf('=', last);
                try {
                    if (eqPos < 0 || eqPos > next)
                        result.put(URLDecoder.decode(qs.substring(last, next), "utf-8"), "");
                    else
                        result.put(URLDecoder.decode(qs.substring(last, eqPos), "utf-8"), URLDecoder.decode(qs.substring(eqPos + 1, next), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e); // will never happen, utf-8 support is mandatory for java
                }
            }
            last = next + 1;
        }
        return result;
    }

    private static void pln(String s) { System.out.println(s); }

}

