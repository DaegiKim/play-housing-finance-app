package actions;

import com.typesafe.config.Config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import utils.MessageApi;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SecuredAction extends Action.Simple {
    @Inject MessageApi messageApi;

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    @Inject Config config;

    @Override
    public CompletionStage<Result> call(Http.Request req) {
        String errorMsg = messageApi.getMessage("error.auth.token.default");
        if(req.header(HEADER_AUTHORIZATION).isPresent()) {
            String jwt = req.header(HEADER_AUTHORIZATION).get();
            jwt = jwt.replace(BEARER, "");

            try {
                Claims claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();
                return delegate.call(req);
            } catch (MalformedJwtException ex) {
                errorMsg = messageApi.getMessage("error.auth.token.default");
            } catch (SignatureException ex) {
                errorMsg = messageApi.getMessage("error.auth.token.invalid_signature");
            } catch (ExpiredJwtException ex) {
                errorMsg = messageApi.getMessage("error.auth.token.expired");
            }
        }
        return CompletableFuture.completedFuture(unauthorized(errorMsg));
    }
}
