package com.ekoapp.auth;

import com.google.gson.JsonObject;

class EkoOAuthHttpException extends Exception {
    EkoOAuthHttpException (JsonObject jsonObject) {
        super(jsonObject.get("status").getAsString() + " " + jsonObject.get("message").getAsString());
    }
    EkoOAuthHttpException (int statusCode, String message) {
        super(statusCode + " " + message);
    }
}
