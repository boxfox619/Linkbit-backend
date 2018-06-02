package com.boxfox.cross.test;

import com.boxfox.cross.wallet.eth.EthereumService;
import org.junit.Test;

public class CoinServiceTest {

    @Test
    public void testGetTransaction(){
        EthereumService service = new EthereumService();
        service.getTransactionList("0xa5B5bE1ecB74696eC27E3CA89E5d940c9dbcCc56");
    }
}
