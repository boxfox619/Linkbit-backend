package com.boxfox.cross.service.wallet.indexing;

import io.vertx.core.Vertx;

public interface IndexingService {
  void indexing(Vertx vertx, String address);
}
