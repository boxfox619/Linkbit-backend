package com.boxfox.linkbit.wallet.eth.erc20;

import io.vertx.core.Vertx;

public class EOSService extends ERC20Service {

  public EOSService(Vertx vertx) {
    super(vertx, "EOS");
  }
}
