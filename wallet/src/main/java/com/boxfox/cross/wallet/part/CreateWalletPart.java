package com.boxfox.cross.wallet.part;

import com.boxfox.cross.wallet.model.WalletCreateResult;

public interface CreateWalletPart {
  WalletCreateResult createWallet(String password);
}
