package com.boxfox.linkbit.wallet.part;

import com.boxfox.linkbit.wallet.model.WalletCreateResult;

public interface CreateWalletPart {
  WalletCreateResult createWallet(String password);
}
