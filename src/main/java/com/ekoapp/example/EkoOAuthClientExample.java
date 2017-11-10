package com.ekoapp.example;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ekoapp.auth.EkoOAuthClient;
import com.ekoapp.auth.EkoOAuthToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
        server.createContext("/cb", new CallBackHandler());
        server.setExecutor(null);
        server.start();
    }

    static class CallBackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Gson gson = new GsonBuilder().create();

            // Extract params from url query
            Map<String, String> params = parseQueryString(t.getRequestURI().getQuery());
            String code = params.get("code");
            String state = params.get("state");

            // Setup client
            EkoOAuthClient authClient = new EkoOAuthClient();
            authClient.setClientId(CLIENT_ID);
            authClient.setClientSecret(CLIENT_SECRET);
            authClient.setRedirectUri(REDIRECT_URI);
            authClient.setEkoUri(EKO_URI);

            String responseMessage;
            try {

                // 1. Request token by authorization code
                EkoOAuthToken token = authClient.requestToken(code);
                pln(" ");
                pln("1. ## Request token by authorization code ##");
                pln("This token is retrieved by authorization code \"" + code + "\"");
                pln(" - access_token: " + token.getAccessToken());
                pln(" - refresh_token: " + token.getRefreshToken());
                pln(" - token_type: " + token.getTokenType());
                pln(" - expires_in: " + token.getExpiresIn());
                pln(" - scope: " + token.getScopes());
                pln(" - id_token: " + token.getRawIdToken());
                pln(" ");

                DecodedJWT tokenId = token.getIdToken();
                pln("And here is the result of decoded id token.");
                pln(" - firstname: " + tokenId.getClaim("firstname").asString());
                pln(" - lastname: " + tokenId.getClaim("lastname").asString());
                pln(" - email: " + tokenId.getClaim("email").asString());
                pln(" - position: " + tokenId.getClaim("position").asString());
                pln(" - iat (issued at): " + tokenId.getIssuedAt());
                pln(" - exp (expires ar): " + tokenId.getExpiresAt());
                pln(" - aud (audience): " + tokenId.getAudience());
                pln(" - iss (issuer): " + tokenId.getIssuer());
                pln("--------------------------------");
                pln(" ");


                // 2. Request token by refresh token
                EkoOAuthToken token2 = authClient.requestTokenByRefreshToken(token.getRefreshToken());
                pln(" ");
                pln("2. ## Request token by refresh token ##");
                pln("This token is retrieved by refresh token \"" + token.getRefreshToken() + "\"");
                pln(" - access_token: " + token2.getAccessToken());
                pln(" - refresh_token: " + token2.getRefreshToken());
                pln(" - token_type: " + token2.getTokenType());
                pln(" - expires_in: " + token2.getExpiresIn());
                pln(" - scope: " + token2.getScopes());
                pln(" - id_token: " + token2.getRawIdToken());
                pln(" ");

                DecodedJWT tokenId2 = token2.getIdToken();
                pln("And here is the result of decoded id token.");
                pln(" - firstname: " + tokenId2.getClaim("firstname").asString());
                pln(" - lastname: " + tokenId2.getClaim("lastname").asString());
                pln(" - email: " + tokenId2.getClaim("email").asString());
                pln(" - position: " + tokenId2.getClaim("position").asString());
                pln(" - iat (issued at): " + tokenId2.getIssuedAt());
                pln(" - exp (expires ar): " + tokenId2.getExpiresAt());
                pln(" - aud (audience): " + tokenId2.getAudience());
                pln(" - iss (issuer): " + tokenId2.getIssuer());
                pln("--------------------------------");
                pln(" ");

                // 3. Get user info by access token
                JsonObject userInfo = authClient.requestUserInfo(token.getAccessToken());
                pln(" ");
                pln("3. ## Get user info by access token ##");
                pln(" - user_id: " + userInfo.get("_id").getAsString());
                pln(" - firstname: " + userInfo.get("firstname").getAsString());
                pln(" - lastname: " + userInfo.get("lastname").getAsString());
                pln(" - email: " + userInfo.get("email").getAsString());
                pln(" - network_id: " + userInfo.get("nid").getAsString());
                pln(" - position: " + userInfo.get("position").getAsString());
                pln(" - status: " + userInfo.get("status").getAsString());
                pln(" - extras: " + gson.toJson(userInfo.get("extras").getAsJsonObject()));

                responseMessage = "200 Ok, please observe the log on your server.";
                t.sendResponseHeaders(200, responseMessage.length());
                OutputStream os = t.getResponseBody();
                os.write(responseMessage.getBytes());
                os.close();
            }
            catch (Exception e) {
                responseMessage = "500 Internal Server Error, please observe the log on your server.";
                e.printStackTrace();
                t.sendResponseHeaders(500, responseMessage.length());
                OutputStream os = t.getResponseBody();
                os.write(responseMessage.getBytes());
                os.close();
            }

        }
    }


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

