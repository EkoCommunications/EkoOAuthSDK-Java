package com.ekoapp.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

public class EkoOAuthClient {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String tokenUri;
    private String userInfoUri;
    private String authenticateUri;
    private String ekoUri;
    private String scope;

    public EkoOAuthClient () {
        this.scope = "openid profile";
    }


    public String requestUserInfoAsJsonString(String accessToken) throws Exception {
        return this.sendUserInfoRequest(accessToken);
    }
    public String requestUserInfoAsJsonStringByCode(String code) throws Exception{
        EkoOAuthToken token = this.requestToken(code);
        return this.requestUserInfoAsJsonString(token.getAccessToken());
    }

    public JsonObject requestUserInfo(String accessToken) throws Exception {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(this.sendUserInfoRequest(accessToken), JsonObject.class);
    }
    public JsonObject requestUserInfoByCode(String code) throws Exception{
        EkoOAuthToken token = this.requestToken(code);
        return this.requestUserInfo(token.getAccessToken());
    }

    private String sendUserInfoRequest(String accessToken) throws Exception {
        Request request = new Request.Builder()
                .header("Authorization", this.getBearerCredentialString(accessToken))
                .url(this.userInfoUri)
                .build();


        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        String result =  response.body().string();
        System.out.println(response);

        Gson gson = new GsonBuilder().create();
        if (response.code() == 200) {
            return result;
        }
        else {
            JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
            throw new EkoOAuthHttpException(jsonObject);
        }
    }

    public String requestTokenAsJsonString(String code) throws Exception {
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", this.redirectUri)
                .build();
        return this.sendTokenRequest(requestBody);
    }
    public String requestTokenAsJsonStringByRefreshToken(String refreshToken) throws Exception {
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("redirect_uri", this.redirectUri)
                .build();
        return this.sendTokenRequest(requestBody);
    }

    public EkoOAuthToken requestToken(String code) throws Exception {
        return this.parseToken(this.requestTokenAsJsonString(code));
    }
    public EkoOAuthToken requestTokenByRefreshToken(String refreshToken) throws Exception {
        return this.parseToken(this.requestTokenAsJsonStringByRefreshToken(refreshToken));
    }

    private String sendTokenRequest(RequestBody requestBody) throws Exception {
        Request request = new Request.Builder()
                .header("Authorization", this.getBasicCredentialString())
                .url(this.tokenUri)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        String result =  response.body().string();
        System.out.println(response);

        Gson gson = new GsonBuilder().create();
        if (response.code() == 200) {
            return result;
        }
        else {
            JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
            throw new EkoOAuthHttpException(jsonObject);
        }
    }

    public String createState() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    public String createAuthenticateUrl(String state) {
        return this.authenticateUri +
                "?response_type=code" +
                "&client_id=" + this.clientId +
                "&redirect_uri=" + this.redirectUri +
                "&scope=" + this.scope +
                "&state=" + state;
    }

    private String getBasicCredentialString() {
        String credential = this.clientId + ":" + this.clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(credential.getBytes());
    }

    private String getBearerCredentialString(String accessToken) {
        String credential = "Bearer " + accessToken;
        return credential;
    }

    private EkoOAuthToken parseToken(String jsonString) throws Exception {
        Gson gson = new GsonBuilder().create();
        EkoOAuthToken oAuthToken = gson.fromJson(jsonString, EkoOAuthToken.class);
        oAuthToken.parseIdTokenString(this.authenticateUri, this.clientSecret);
        oAuthToken.parseScopeString();
        return oAuthToken;
    }

    public void validateSate(String cachedState, String receivedState) throws Exception {
        if(receivedState.isEmpty())
            throw new EkoOAuthHttpException(500, "State must not be empty.");

        if(!cachedState.equals(receivedState))
            throw new EkoOAuthHttpException(500, "Invalid state.");
    }



    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    private void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    private void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }

    private void setAuthenticateUri(String authenticateUri) {
        this.authenticateUri = authenticateUri;
    }

    public String getEkoUri() {
        return ekoUri;
    }

    public void setEkoUri(String ekoUri) {
        this.ekoUri = ekoUri;
        this.setAuthenticateUri(this.ekoUri + "/oauth/authorize");
        this.setTokenUri(this.ekoUri + "/oauth/token");
        this.setUserInfoUri(this.ekoUri + "/userinfo");
    }
}
