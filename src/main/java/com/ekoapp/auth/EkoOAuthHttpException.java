package com.ekoapp.auth;

import com.google.gson.JsonObject;

public class EkoOAuthHttpException extends Exception {
    public EkoOAuthHttpException (JsonObject jsonObject) {
        super(jsonObject.get("status").getAsString() + " " + jsonObject.get("message").getAsString());
    }
}
