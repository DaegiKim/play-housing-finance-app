package exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.HashMap;

public class FinanceRuntimeException extends RuntimeException {
    public enum ErrorCode {
        /* -1 ~ -100 ===> 400 Bad Request */
        INVALID_PARAMETER(-1),
        USERNAME_DUPLICATE(-2),
        PASSWORD_NOT_EQUALS(-3),
        ALREADY_REGISTERED_CSV_FILE(-4),

        /* -101 ~ -200 ===> 401 Unauthorized */
        AUTH_TOKEN_INVALID(-101),
        AUTH_TOKEN_EXPIRED(-102),
        AUTH_TOKEN_INVALID_SIGNATURE(-103),

        /* -201 ~ -300 ===> 403 Forbidden */

        /* -301 ~ -400 ===> 404 Not Found */
        USERNAME_NOT_FOUND(-301),
        BANK_NOT_FOUND(-302),

        /* -401 ~ -500 ===> 500 Internal Server Error */
        CSV_IO_EXCEPTION(-401);

        private int code;
        ErrorCode(int code) {
            this.code = code;
        }
    }

    private ErrorCode errorCode;

    public FinanceRuntimeException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public Result getResult() {
        if(this.errorCode.code >= -100) {
            return Results.status(Http.Status.BAD_REQUEST, getErrorMessageBody());
        }
        else if(this.errorCode.code >= -200) {
            return Results.status(Http.Status.UNAUTHORIZED, getErrorMessageBody());
        }
        else if(this.errorCode.code >= -300) {
            return Results.status(Http.Status.FORBIDDEN, getErrorMessageBody());
        }
        else if(this.errorCode.code >= -400) {
            return Results.status(Http.Status.NOT_FOUND, getErrorMessageBody());
        }

        return Results.status(Http.Status.INTERNAL_SERVER_ERROR, getErrorMessageBody());
    }

    private JsonNode getErrorMessageBody() {
        int errorCode = this.errorCode.code;
        String errorMessage = this.errorCode.name();

        return Json.toJson(new HashMap<String, Object>() {{
            put("error_code", errorCode);
            put("error_message", errorMessage);
        }});
    }
}
