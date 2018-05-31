package com.boxfox.cross.service.wallet.indexing;

import static com.boxfox.cross.service.wallet.indexing.IndexingMessage.EVENT_SUBJECT;

import com.boxfox.cross.service.wallet.WalletServiceManager;
import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;

public class TransactionIndexingVerticle extends AbstractVerticle {

  @Override
  public void start() {
    vertx.eventBus().<String>consumer(EVENT_SUBJECT, message -> {
      System.out.println(message);
      Gson gson = new Gson();
      IndexingMessage msg =  gson.fromJson(message.body(), IndexingMessage.class);
      IndexingService service = WalletServiceManager.getService(msg.getSymbol()).getIndexingService();
      if(service!=null){
        service.indexing(vertx, msg.getAddress());
      }
    });
  }
}