package actions;

import com.typesafe.config.Config;
import exceptions.FinanceRuntimeException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class SecuredAction extends Action.Simple {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private Config config;

    @Inject
    SecuredAction(Config config) {
        this.config = config;
    }

    @Override
    public CompletionStage<Result> call(Http.Request req) {
        if(req.header(HEADER_AUTHORIZATION).isPresent()) {
            String jwt = req.header(HEADER_AUTHORIZATION).get();
            jwt = jwt.replace(BEARER, "");

            try {
                Claims claims = Jwts.parser().setSigningKey(config.getString("play.http.secret.key")).parseClaimsJws(jwt).getBody();
                return delegate.call(req);
            } catch (MalformedJwtException ex) {
                throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.AUTH_TOKEN_INVALID);
            } catch (SignatureException ex) {
                throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.AUTH_TOKEN_INVALID_SIGNATURE);
            } catch (ExpiredJwtException ex) {
                throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.AUTH_TOKEN_EXPIRED);
            }
        }

        throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.AUTH_TOKEN_INVALID);
    }
}
