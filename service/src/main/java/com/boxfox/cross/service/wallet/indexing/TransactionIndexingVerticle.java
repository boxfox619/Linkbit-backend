package com.boxfox.cross.service.wallet.indexing;

import io.vertx.core.AbstractVerticle;

public class TransactionIndexingVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.eventBus().<String>consumer("sample.data", message -> {
      System.out.println("[Worker] Consuming data in " + Thread.currentThread().getName());
      String body = message.body();
      message.reply(body.toUpperCase());
    });
  }
}