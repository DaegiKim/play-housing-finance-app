package controllers;

import actions.SecuredAction;
import com.fasterxml.jackson.databind.JsonNode;
import exceptions.FinanceRuntimeException;
import models.Bank;
import models.Finance;
import play.libs.Json;
import play.mvc.*;

@With(SecuredAction.class)
public class FinanceController extends Controller {
    public Result init() {
        if(Finance.init())
            return ok(Json.newObject().put("message", "succeed"));
        else
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.ALREADY_REGISTERED_CSV_FILE);
    }

    public Result bankList() {
        return ok(Bank.getBankList());
    }

    public Result getSummaryByYearly() {
        return ok(Bank.getSummaryByYearly());
    }

    public Result getMaximumByYearly() {
        return ok(Bank.getMaximumByYearly());
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getMaxMinByYearly(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();

        if(jsonNode==null || !jsonNode.hasNonNull("bank"))
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.INVALID_PARAMETER);

        String bank = jsonNode.get("bank").asText();

        return ok(Bank.getMaxMinByYearly(bank));
    }

    public Result getForecast(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();
        String bankName = jsonNode.get("bank").asText();
        int month = jsonNode.get("month").asInt();

        return ok(Finance.getForecast(bankName, month));
    }
}
