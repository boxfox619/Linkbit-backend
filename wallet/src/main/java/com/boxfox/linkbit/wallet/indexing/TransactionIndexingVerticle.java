package com.boxfox.linkbit.wallet.indexing;

import static com.boxfox.linkbit.wallet.indexing.IndexingMessage.EVENT_SUBJECT;

import com.boxfox.linkbit.wallet.WalletServiceManager;
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
          IndexingService service = WalletServiceManager.getService(msg.getSymbol()).getIndexingService();
          if (service != null) {
              System.out.println("start indexing : " + msg.getSymbol());
              service.indexing(vertx, msg.getAddress()).setHandler(e->{
                  if(e.succeeded()){
                      System.out.println("success indexing : " + msg.getSymbol()+" - "+msg.getAddress());
                  }else{
                      System.out.println("fail indexing : " + msg.getSymbol()+" - "+msg.getAddress());
                  }
              });
          }
      });
  }
}