package com.boxfox.cross.wallet;

import com.boxfox.cross.wallet.eth.EthereumService;
import io.vertx.core.Vertx;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;

public class WalletServiceTest {

  @Test
  public void test(){
    EthereumService ethereumService = new EthereumService(Vertx.vertx());
    ethereumService.init();
    WalletServiceManager.register("ETH", ethereumService);
  }

  @Test
  public void createEthWallet() throws InterruptedException {
    String uid = "vKEVPGh2r4h0dVpuONLuZ4Uwuh02";
    String name = "테스트 지갑";
    String password = "testpw231";
    String description = "desccas";
    CountDownLatch latch = new CountDownLatch(1);
    WalletServiceManager.getService("eth")
        .createWallet(uid, name, password, description, false, false, res -> {
          System.out.println(res.result());
          latch.countDown();
        });
    latch.await();
  }

}
