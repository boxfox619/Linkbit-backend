package com.boxfox.linkbit.wallet.indexing;

import static com.boxfox.linkbit.wallet.indexing.IndexingMessage.EVENT_SUBJECT;

import com.boxfox.linkbit.wallet.WalletService;
import com.boxfox.linkbit.wallet.WalletServiceRegistry;
import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;

public class TransactionIndexingVerticle extends AbstractVerticle {

  @Override
  public void start() {
      MessageConsumer<String> consumer = vertx.eventBus().consumer(EVENT_SUBJECT);
      consumer.handler(message -> {
          Gson gson = new Gson();
          IndexingMessage msg = gson.fromJson(message.body(), IndexingMessage.class);
          System.out.println("request indexing : " + msg.getSymbol());
          WalletService service = WalletServiceRegistry.getService(msg.getSymbol());
          if (service != null) {
              System.out.println("start indexing : " + msg.getSymbol());
              service.indexingTransaction(msg.getAddress());
              System.out.println("finish indexing : " + msg.getSymbol());
          }
      });
  }
}