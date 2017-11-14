package com.ekoapp.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.annotations.SerializedName;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class EkoOAuthToken {

    @SerializedName("id_token") private String idToken;
    private DecodedJWT _idToken;

    @SerializedName("access_token") private String _accessToken;

    @SerializedName("refresh_token") private String _refreshToken;

    @SerializedName("token_type") private String _tokenType;

    @SerializedName("expires_in") private int _expiresIn;

    @SerializedName("scope") private String scope;
    private List<String> _scopes;

    void parseIdTokenString(String authenticateUri, String clientSecret) throws Exception {
        try {
            Algorithm algorithm = Algorithm.HMAC256(clientSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(authenticateUri)
                    .build();
            this._idToken = verifier.verify(this.idToken);
        }
        catch (UnsupportedEncodingException exception) {
            throw exception;
        }
        catch (JWTVerificationException exception) {
            throw exception;
        }
    }

    void parseScopeString() throws Exception {
        this._scopes = Arrays.asList(this.scope.split("\\s+"));
    }



    @Override
    public String toString() {
        return "EkoOAuthToken{" +
                "idToken='" + idToken + '\'' +
                ", idToken=" + _idToken +
                ", accessToken='" + _accessToken + '\'' +
                ", refreshToken='" + _refreshToken + '\'' +
                ", tokenType='" + _tokenType + '\'' +
                ", expiresIn=" + _expiresIn +
                ", scope='" + scope + '\'' +
                ", scopes=" + _scopes +
                '}';
    }

    public boolean isInScope(String scopeName) {
        return this.getScopes().contains(scopeName);
    }

    public String getRawIdToken() {
        return this.idToken;
    }

    public DecodedJWT getIdToken() {
        return this._idToken;
    }

    public String getAccessToken() {
        return this._accessToken;
    }

    public String getRefreshToken() {
        return this._refreshToken;
    }

    public String getTokenType() {
        return this._tokenType;
    }

    public int getExpiresIn() {
        return this._expiresIn;
    }

    public List<String> getScopes() {
        return this._scopes;
    }

}
