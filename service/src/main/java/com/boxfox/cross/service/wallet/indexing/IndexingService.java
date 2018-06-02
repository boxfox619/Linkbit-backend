package com.boxfox.cross.service.wallet.indexing;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

public interface IndexingService {
  Future<Void> indexing(Vertx vertx, String address);
}
