import exceptions.FinanceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.http.HttpErrorHandler;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@Singleton
public class ErrorHandler implements HttpErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
        return CompletableFuture.completedFuture(Results.status(statusCode, "A client error occurred: " + message));
    }

    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        logging(request, exception);

        if(exception instanceof CompletionException) {
            CompletionException completionException = (CompletionException) exception;
            if(completionException.getCause() != null && completionException.getCause() instanceof FinanceRuntimeException) {
                FinanceRuntimeException financeRuntimeException = (FinanceRuntimeException) completionException.getCause();
                return CompletableFuture.completedFuture(financeRuntimeException.getResult());
            }
        }
        else if(exception instanceof FinanceRuntimeException) {
            FinanceRuntimeException financeRuntimeException = (FinanceRuntimeException) exception;
            return CompletableFuture.completedFuture(financeRuntimeException.getResult());
        }

        return CompletableFuture.completedFuture(Results.internalServerError("A server error occurred: " + exception.getMessage()));
    }

    private void logging(RequestHeader request, Throwable exception) {
        log.error(
                "{} {} {}",
                request.method(),
                request.uri(),
                exception.getMessage());
    }
}