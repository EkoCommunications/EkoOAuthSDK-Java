package com.ekoapp.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.util.Base64;

public class EkoOAuthClient {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String tokenUri;
    private String userInfoUri;
    private String issuerUri;
    private String ekoUri;

    public EkoOAuthClient () {
    }

    /**
     * Get OAuth Token by authorization code
     * @param code authentication_code
     * @return oauth token
     * @throws Exception
     */
    public EkoOAuthToken requestToken(String code) throws Exception {
        RequestBody requestBody = new FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", this.redirectUri)
            .build();
        return this.sendTokenRequest(requestBody);
    }

    /**
     * Get OAuth Token by refresh token
     * @param refreshToken
     * @return oauth token
     * @throws Exception
     */
    public EkoOAuthToken requestTokenByRefreshToken(String refreshToken) throws Exception {
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("redirect_uri", this.redirectUri)
                .build();
        return this.sendTokenRequest(requestBody);
    }

    public JsonObject requestUserInfo(String accessToken) throws Exception {
        try {

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
                JsonObject userInfo = gson.fromJson(result, JsonObject.class);
                return userInfo;
            }
            else {
                JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                throw new EkoOAuthHttpException(jsonObject);
            }
        }
        catch (Exception e) {
            throw e;
        }
    }

    private EkoOAuthToken sendTokenRequest(RequestBody requestBody) throws Exception {
        try {
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
                EkoOAuthToken oAuthToken = gson.fromJson(result, EkoOAuthToken.class);
                oAuthToken.parseIdTokenString(this.issuerUri, this.clientSecret);
                oAuthToken.parseScopeString();
                return oAuthToken;
            }
            else {
                JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                throw new EkoOAuthHttpException(jsonObject);
            }
        }
        catch (Exception e) {
            throw e;
        }
    }

    private String getBasicCredentialString() {
        String credential = this.clientId + ":" + this.clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(credential.getBytes());
    }

    private String getBearerCredentialString(String accessToken) {
        String credential = "Bearer " + accessToken;
        return credential;
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

    private void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getEkoUri() {
        return ekoUri;
    }

    public void setEkoUri(String ekoUri) {
        this.ekoUri = ekoUri;
        this.setIssuerUri(this.ekoUri + "/oauth/authorize");
        this.setTokenUri(this.ekoUri + "/oauth/token");
        this.setUserInfoUri(this.ekoUri + "/userinfo");
    }
}
