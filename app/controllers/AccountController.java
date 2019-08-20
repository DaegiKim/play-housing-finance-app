package controllers;

import actions.SecuredAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import exceptions.FinanceRuntimeException;
import io.ebean.DuplicateKeyException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import models.User;
import play.libs.Json;
import play.mvc.*;
import utils.BCrypt;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class AccountController extends Controller {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private Config config;

    @Inject
    AccountController(Config config){
        this.config = config;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result signUp(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();

        if(jsonNode==null || !jsonNode.hasNonNull("username") || !jsonNode.hasNonNull("password"))
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.INVALID_PARAMETER);

        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();

        User user = new User();

        user.username = username;
        user.password = BCrypt.hashpw(password, BCrypt.gensalt());

        try  {
            user.save();
        } catch (DuplicateKeyException ex) {
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.USERNAME_DUPLICATE);
        }

        String jwt = generateJWT(user.username, user.password);
        Claims claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();

        return ok(Json.newObject()
                .put("accessToken", jwt)
                .put("issued_at", claims.getIssuedAt().toString())
                .put("expires_in", claims.getExpiration().toString())
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result signIn(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();

        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();

        User user = User.findByUsername(username);

        if(user == null)
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.USERNAME_NOT_FOUND);

        if(!BCrypt.checkpw(password, user.password))
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.PASSWORD_NOT_EQUALS);

        String jwt = generateJWT(user.username, user.password);
        Claims claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();

        return ok(Json.newObject()
                .put("accessToken", jwt)
                .put("issued_at", claims.getIssuedAt().toString())
                .put("expires_in", claims.getExpiration().toString())
        );
    }

    @With(SecuredAction.class)
    public Result refresh(Http.Request request) {
        String jwt = request.header(HEADER_AUTHORIZATION).get().replaceAll(BEARER, "");
        Claims claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();

        String username = claims.get("username", String.class);
        String password = claims.get("password", String.class);

        User user = User.findByUsername(username);

        if(user == null || !user.username.equals(username) || !user.password.equals(password))
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.AUTH_TOKEN_INVALID);

        jwt = generateJWT(user.username, user.password);
        claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();

        return ok(Json.newObject()
                .put("accessToken", jwt)
                .put("issued_at", claims.getIssuedAt().toString())
                .put("expires_in", claims.getExpiration().toString())
        );
    }

    private String generateJWT(String username, String password) {
        long expiresIn = config.getLong("app.auth.token.expires.in");

        return Jwts.builder()
                .claim("username", username)
                .claim("password", password)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(expiresIn).toInstant()))
                .signWith(SignatureAlgorithm.HS256, config.getString("play.http.secret.key"))
                .compact();
    }
}
