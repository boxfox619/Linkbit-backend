package com.boxfox.linkbit.wallet.eth;

import com.boxfox.linkbit.common.RoutingException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class EthereumWithdrawTest {

    @Test
    public void withdrawTest() throws RoutingException {
        EthereumServiceContext context = EthereumServiceContext.create();
        String targetAddress = "";
        JsonObject data = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add("century");
        arr.add("world");
        arr.add("coach");
        arr.add("document");
        arr.add("bird");
        arr.add("sick");
        arr.add("cargo");
        arr.add("claim");
        arr.add("attitude");
        arr.add("mosquito");
        arr.add("copper");
        arr.add("asthma");
        StringBuilder dmnemonic = new StringBuilder();
        for (Object d : arr) {
            dmnemonic.append((String) d);
        }
        data.put("type", "mnemonic");
        data.put("password", "crosstest1212");
        data.put("mnemonic", dmnemonic.toString());
        context.getTransactionPart().send(data, targetAddress, "0.00001");
    }
}
