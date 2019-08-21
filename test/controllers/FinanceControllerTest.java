package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FinanceControllerTest {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    Application app = fakeApplication();
    static String accessToken;

    @BeforeClass
    public static void getToken() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri("/api/auth/signup");

        request = request.bodyJson(
                Json.newObject()
                        .put("username", "financeuser")
                        .put("password", "1234")
        );

        Result result = route(fakeApplication(), request);

        String accessToken = Json.parse(contentAsString(result)).get("accessToken").asText();

        FinanceControllerTest.accessToken = accessToken;
    }

    @Test
    public void _1_initTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri("/api/finance/init")
                .header(HEADER_AUTHORIZATION, BEARER + accessToken);

        Result result = route(app, request);

        assertEquals(OK, result.status());
    }

    @Test
    public void _2_listTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/api/finance/list")
                .header(HEADER_AUTHORIZATION, BEARER + accessToken);

        Result result = route(app, request);

        assertEquals(OK, result.status());

        JsonNode parse = Json.parse(contentAsString(result));

        assertEquals(9, parse.size());
    }

    @Test
    public void _3_summaryByYearlyTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/api/finance/summary-by-yearly")
                .header(HEADER_AUTHORIZATION, BEARER + accessToken);

        Result result = route(app, request);

        assertEquals(OK, result.status());

        JsonNode parse = Json.parse(contentAsString(result));

        assertEquals(13, parse.get("data").size());
    }

    @Test
    public void _4_maximumByYearlyTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/api/finance/maximum-by-yearly")
                .header(HEADER_AUTHORIZATION, BEARER + accessToken);

        Result result = route(app, request);

        assertEquals(OK, result.status());

        JsonNode parse = Json.parse(contentAsString(result));

        assertEquals("주택도시기금", parse.get("bank").asText());
    }

    @Test
    public void _5_maxMinByYearlyTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/api/finance/max-min-by-yearly")
                .header(HEADER_AUTHORIZATION, BEARER + accessToken)
                .bodyJson(Json.newObject().put("bank", "하나은행"));

        Result result = route(app, request);

        assertEquals(OK, result.status());

        JsonNode parse = Json.parse(contentAsString(result));

        assertEquals(2, parse.get("support_amount").size());
    }

    @Test
    public void _6_forecastTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/api/finance/forecast")
                .header(HEADER_AUTHORIZATION, BEARER + accessToken)
                .bodyJson(
                        Json.newObject()
                                .put("bank", "국민은행")
                                .put("month", 2)
                );

        Result result = route(app, request);

        assertEquals(OK, result.status());

        JsonNode parse = Json.parse(contentAsString(result));

        assertEquals(4, parse.size());
    }
}
