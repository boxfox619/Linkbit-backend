package com.boxfox.core.router;

import com.boxfox.cross.service.wallet.WalletServiceManager;
import com.boxfox.cross.wallet.eth.EthereumService;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

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
        WalletServiceManager.register("eth", new EthereumService());
    }

    @Test
    public void getBalance() {
        HttpServerResponse response = createResponse(200);
        when(response.write(anyString())).then(invocation -> {
            double balance = Double.valueOf((String) invocation.getArguments()[0]);
            Assert.assertTrue(balance > 0);
            return response;
        });
        this.router.getBalance(ctx, "eth", "0xa5B5bE1ecB74696eC27E3CA89E5d940c9dbcCc56");
    }

    @Test
    public void create() {
        HttpServerResponse response = createResponse(200);
        when(response.write(anyString())).then(invocation -> {
            JsonObject result = new JsonObject((String) invocation.getArguments()[0]);
            Assert.assertTrue(result.getBoolean("result"));
            Assert.assertNotNull(result.getString("name"));
            Assert.assertNotNull(result.getJsonObject("wallet"));
            return response;
        });
        this.router.create(ctx, "testpassword");
    }

    @Test
    public void send() {
        HttpServerResponse response = createResponse(200);
        when(response.write(anyString())).then(invocation -> {
            JsonObject result = new JsonObject((String) invocation.getArguments()[0]);
            Assert.assertTrue(result.getBoolean("result"));
            Assert.assertNotNull(result.getString("transaction"));
            return response;
        });
        try {
            String testWalletFileName = "UTC--2018-05-12T14-33-21.216844100Z--d37821916c2351208f9560f596b0432665083984.json";
            String targetAddress = "0xa5B5bE1ecB74696eC27E3CA89E5d940c9dbcCc56";
            String testpassword = "testpassword";
            URL walletFile = Resources.getResource(testWalletFileName);
            String walletFileText = Resources.toString(walletFile, Charsets.UTF_8);
            this.router.send(ctx, testWalletFileName, walletFileText, testpassword, targetAddress, "1");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private HttpServerResponse createResponse(int successStatusCode) {
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
