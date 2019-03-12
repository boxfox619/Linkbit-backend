package com.boxfox.linkbit.wallet.eth;

import org.junit.Assert;
import org.junit.Test;

public class EthereumBalancePartTest {

    @Test
    public void getBalanceTest() {
        String address = "0xf9a35fe66424dbed626f6c6e77a4749a4937dd8d";
        EthereumServiceContext context = EthereumServiceContext.create();
        double balance = context.getBalancePart().getBalance(address);
        Assert.assertTrue(balance > 0);
    }
}
