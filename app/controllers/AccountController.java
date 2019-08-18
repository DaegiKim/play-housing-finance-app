package controllers;

import actions.SecuredAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import io.jsonwebtoken.*;
import models.User;
import play.libs.Json;
import play.mvc.*;
import utils.BCrypt;
import utils.MessageApi;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

public class AccountController extends Controller {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    @Inject MessageApi messageApi;
    @Inject Config config;

    @BodyParser.Of(BodyParser.Json.class)
    public Result signUp(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();

        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();

        User user = new User();
        user.username = username;
        user.password = BCrypt.hashpw(password, BCrypt.gensalt());
        user.save();

        String jwt = generateJWT(user.username, user.password);
        Claims claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();

        ObjectNode result = Json.newObject();
        result.put("accessToken", jwt);
        result.put("issued_at", claims.getIssuedAt().toString());
        result.put("expires_in", claims.getExpiration().toString());

        return ok(result);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result signIn(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();

        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();

        User user = User.findByUsername(username);

        if(user == null) {
            return forbidden(messageApi.getMessage("error.auth.username.invalid", username));
        } else if(!BCrypt.checkpw(password, user.password)) {
            return forbidden("invalid password");
        }

        String jwt = generateJWT(user.username, user.password);
        Claims claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();

        ObjectNode result = Json.newObject();
        result.put("accessToken", jwt);
        result.put("issued_at", claims.getIssuedAt().toString());
        result.put("expires_in", claims.getExpiration().toString());

        return ok(result);
    }

    @With(SecuredAction.class)
    public Result refresh(Http.Request request) {
        Optional<String> token = request.header(HEADER_AUTHORIZATION);
        if(token.isPresent()) {
            String jwt = token.get().replaceAll(BEARER, "");

            Claims claims;
            try {
                claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();
            } catch (MalformedJwtException ex) {
                return forbidden("invalid token");
            } catch (ExpiredJwtException ex) {
                return forbidden("expired token");
            }

            String username = claims.get("username", String.class);
            String password = claims.get("password", String.class);

            User user = User.findByUsername(username);

            if(user == null || !user.username.equals(username) || !user.password.equals(password)) {
                return forbidden("invalid token");
            }

            jwt = generateJWT(user.username, user.password);
            claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();

            ObjectNode result = Json.newObject();
            result.put("accessToken", jwt);
            result.put("issued_at", claims.getIssuedAt().toString());
            result.put("expires_in", claims.getExpiration().toString());

            return ok(result);
        } else {
            return forbidden("Please provide a token.");
        }
    }

    private String generateJWT(String username, String password) {
        String jwt = Jwts.builder()
                .claim("username", username)
                .claim("password", password)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(10).toInstant()))
                .signWith(SignatureAlgorithm.HS256, config.getString("play.http.secret.key"))
                .compact();

        return jwt;
    }
}
