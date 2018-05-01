package com.boxfox.core.router;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WalletRouterTest {

    private WalletRouter router;
    private RoutingContext ctx;

    public WalletRouterTest() {
        this.router = new WalletRouter();
        this.ctx = mock(RoutingContext.class);
    }

    @Test
    public void getBalance() {
        HttpServerResponse response = createResponse(200);
        when(response.write(anyString())).then(invocation -> {
            int balance = Integer.valueOf((String) invocation.getArguments()[0]);
            Assert.assertTrue(balance > 0);
            return response;
        });
        this.router.getBalance(ctx, "0xa5B5bE1ecB74696eC27E3CA89E5d940c9dbcCc56");
    }

    @Test
    public void create() {
        HttpServerResponse response = createResponse(200);
        when(response.write(anyString())).then(invocation -> {
            JsonObject result = new JsonObject((String) invocation.getArguments()[0]);
            Assert.assertTrue(result.getBoolean("result"));
            Assert.assertNotNull(result.getJsonObject("name"));
            Assert.assertNotNull(result.getJsonObject("wallet"));
            return response;
        });
        this.router.create(ctx, "testpassword");
    }

    private HttpServerResponse createResponse(int successStatusCode){
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(ctx.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).then(invocation -> {
            int statusCode = (int) invocation.getArguments()[0];
            Assert.assertEquals(statusCode, successStatusCode);
            return response;
        });
        return response;
    }
}
