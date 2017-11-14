# EkoOAuthSDK-Java

An OAuth authentication client for integrating 3rd party application with Eko App.


### Prerequisites

Client application must be registered with Eko first. They must provide a `redirect_uri` and they will received `client_id`, `client_secret` and `eko_uri`.


### Running Example

1. Edit the `client_id`, `client_secret`, `redirect_uri` and `eko_uri` in `EkoOAuthClientExample.java` to be matched with the value you registerd.
2. Run EkoOAuthClientExample.java.
3. Go to `http://localhost:12345/app` via the browser.


### Usage

See `EkoOAuthClientExample.java` for the complete flow of usage.

#### 1. Initialization
```java
EkoOAuthClient authClient = new EkoOAuthClient();
authClient.setClientId(CLIENT_ID);
authClient.setClientSecret(CLIENT_SECRET);
authClient.setRedirectUri(REDIRECT_URI);
authClient.setEkoUri(EKO_URI);
```


#### 2. Authentication
To authenticate a user, the client application must redirect the user to the url below.

- Create a redirect url to Eko authentication endpoint
```java
String state = authClient.createState();
// DO NOT forget to store this state in the session to validate it when Eko redirect back to your endpoint

String authEndpointUrl = authClient.createAuthenticateUrl(state);
```


#### 3. Get token and user info
The client application must setup an endpoint which must match the predefined `redirect_uri`. After authentication success or fail, Eko will redirect the user back to this `redirect_uri` endpoint along with `state` and `code`  (authentication code) as query parameters. The client application must validate the incoming state with the one previously store on the session. Then, the client application use the `code` to retreive access token and use the access token to retreive user info. DO NOT use the `code` if state validation fail.


- Validate state (if fail, exceptions will be thrown)
```java
authClient.validateSate(session.get("state"), receivedState);
```


- Get token
```java
EkoOAuthToken token = authClient.requestToken(code);
```
```java
// Retreiving values from access token
String accessToken = token.getAccessToken();
String refreshToken = token.getRefreshToken();
String tokenType = token.getTokenType();
String expiresIn = token.getExpiresIn();
List<String> scopes = token.getScopes();
String idTokenString = token.getRawIdToken();
DecodedJWT idToken = token.getIdToken();
```


- Get ID token, please refer to [java-jwt](https://github.com/auth0/java-jwt) on how to maipulate `DecodedJWT` object.
```java
DecodedJWT idToken = token.getIdToken();
```
```java
// Retreiving values from id token
String firstName = idToken.getClaim("firstname").asString();
String lastName = idToken.getClaim("lastname").asString();
String email = idToken.getClaim("email").asString();
```


- Get user info, please refer to [gson](https://github.com/google/gson) on how to maipulate `JsonObject` object.
```java
JsonObject userInfo = authClient.requestUserInfo(token.getAccessToken());
```
```java
// Retreiving values from user info
String userId = userInfo.get("_id").getAsString();
String firstName = userInfo.get("firstname").getAsString();
String lastName = userInfo.get("lastname").getAsString();
String email = userInfo.get("email").getAsString();
String networkId = userInfo.get("nid").getAsString();
```


or a shortcut to retreive user info ...
- Get user info by `code`
```java
JsonObject userInfo = authClient.requestUserInfoByCode(code);
```

#### 4. Refresh Token

```java
EkoOAuth newToken = authClient.requestTokenByRefreshToken(token.getRefreshToken()) 
```


## Authors

* **Jura Boonnom** - *Initial work* - [jura-b](https://github.com/jura-b)

