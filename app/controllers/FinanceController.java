package controllers;

import actions.SecuredAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Bank;
import models.Finance;
import play.mvc.*;

@With(SecuredAction.class)
public class FinanceController extends Controller {
    public Result init() {
        Finance.init();
        return ok("init: ok");
    }

    /**
     * 주택금융 공급 금융기관(은행) 목록을 출력하는 API 를 개발하세요.
     * GET /api/finance/list
     */
    public Result bankList() {
        return ok(Bank.getBankList());
    }

    /**
     * 년도별 각 금융기관의 지원금액 합계를 출력하는 API 를 개발하세요.
     * GET /api/finance/by-yearly-summary
     */
    public Result getSummaryByYearly() {
        return ok(Finance.getSummaryByYearly());
    }

    /**
     * 각 년도 별 각 기관의 전체 지원 금액 중에서 가장 큰 금액의 기관명을 출력하는 API 개발
     * GET /api/finance/by-yearly-maximum
     */
    public Result getMaximumByYearly() {
        return ok(Bank.getMaximumByYearly());
    }

    /**
     *
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result getMaxMinByYearly(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();
        String bank = jsonNode.get("bank").asText();

        ObjectNode resultNode = Bank.getMaxMinByYearly(bank);

        return ok(resultNode);
    }
}
