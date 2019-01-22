package com.boxfox.linkbit.wallet.part;

import com.boxfox.linkbit.wallet.model.WalletCreateResult;
import io.vertx.core.json.JsonObject;

public interface CreateWalletPart {
  WalletCreateResult createWallet(String password);
  WalletCreateResult importWallet(String type, JsonObject data);
}
