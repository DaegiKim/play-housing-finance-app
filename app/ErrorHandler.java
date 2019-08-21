import exceptions.FinanceRuntimeException;
import play.http.HttpErrorHandler;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class ErrorHandler implements HttpErrorHandler {
    public CompletionStage<Result> onClientError(
            RequestHeader request, int statusCode, String message) {
        return CompletableFuture.completedFuture(
                Results.status(statusCode, "A client error occurred: " + message));
    }

    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        if(exception instanceof FinanceRuntimeException) {
            FinanceRuntimeException financeRuntimeException = (FinanceRuntimeException) exception;
            return CompletableFuture.completedFuture(financeRuntimeException.getResult());
        } else {
            return CompletableFuture.completedFuture(
                    Results.internalServerError("A server error occurred: " + exception.getMessage()));
        }
    }
}