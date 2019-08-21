package controllers;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountControllerTest  {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    Application app = fakeApplication();

    @Test
    public void _1_signUpTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri("/api/auth/signup");

        request = request.bodyJson(
                Json.newObject()
                        .put("username", "test")
                        .put("password", "1234")
        );

        Result result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(contentAsString(result), containsString("accessToken"));
    }

    @Test
    public void _2_signInTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri("/api/auth/signin");

        request = request.bodyJson(
                Json.newObject()
                        .put("username", "test")
                        .put("password", "1234")
        );

        Result result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(contentAsString(result), containsString("accessToken"));
    }

    @Test
    public void _3_refreshTest() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri("/api/auth/signin");

        request = request.bodyJson(
                Json.newObject()
                        .put("username", "test")
                        .put("password", "1234")
        );

        Result result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(contentAsString(result), containsString("accessToken"));

        String accessToken = Json.parse(contentAsString(result)).get("accessToken").asText();

        request = new Http.RequestBuilder()
                .method(PUT)
                .uri("/api/auth/refresh")
                .header(HEADER_AUTHORIZATION, BEARER + accessToken);

        result = route(app, request);

        assertEquals(OK, result.status());
        assertThat(contentAsString(result), containsString("accessToken"));
    }
}
