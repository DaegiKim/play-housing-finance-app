package controllers;

import actions.SecuredAction;
import com.fasterxml.jackson.databind.JsonNode;
import exceptions.FinanceRuntimeException;
import play.libs.Json;
import play.mvc.*;
import services.BankService;
import services.FinanceService;

import javax.inject.Inject;

@With(SecuredAction.class)
public class FinanceController extends Controller {
    private final FinanceService financeService;
    private final BankService bankService;

    @Inject
    FinanceController(FinanceService financeService, BankService bankService) {
        this.financeService = financeService;
        this.bankService = bankService;
    }

    public Result init() {
        if(financeService.init())
            return ok(Json.newObject().put("message", "succeed"));
        else
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.ALREADY_REGISTERED_CSV_FILE);
    }

    public Result bankList() {
        return ok(bankService.getBankList());
    }

    public Result getSummaryByYearly() {
        return ok(bankService.getSummaryByYearly());
    }

    public Result getMaximumByYearly() {
        return ok(bankService.getMaximumByYearly());
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getMaxMinByYearly(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();

        if(jsonNode==null || !jsonNode.hasNonNull("bank"))
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.INVALID_PARAMETER);

        String bank = jsonNode.get("bank").asText();

        return ok(bankService.getMaxMinByYearly(bank));
    }

    public Result getForecast(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();
        String bankName = jsonNode.get("bank").asText();
        int month = jsonNode.get("month").asInt();

        return ok(financeService.getForecast(bankName, month));
    }
}
