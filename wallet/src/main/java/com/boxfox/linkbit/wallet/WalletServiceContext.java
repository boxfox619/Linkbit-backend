package com.boxfox.linkbit.wallet;

import com.boxfox.linkbit.wallet.indexing.IndexingService;
import com.boxfox.linkbit.wallet.model.WalletCreateResult;
import com.boxfox.linkbit.wallet.part.BalancePart;
import com.boxfox.linkbit.wallet.part.CreateWalletPart;
import com.boxfox.linkbit.wallet.part.TransactionPart;
import io.vertx.core.json.JsonObject;

public class WalletServiceContext {
    private IndexingService indexingService;
    protected final String symbol;

    public WalletServiceContext(String symbol){
        this.symbol = symbol;
    }

    protected BalancePart getBalancePart() {
        return address -> 0;
    }

    protected CreateWalletPart getCreateWalletPart() {
        return new CreateWalletPart() {
            @Override
            public WalletCreateResult createWallet(String password) {
                return null;
            }

            @Override
            public WalletCreateResult importWallet(String type, JsonObject data) {
                return null;
            }
        };
    }

    protected TransactionPart getTransactionPart() {
        return null;
    }

    protected IndexingService getIndexingService() {
        return this.indexingService;
    }

    public void setIndexingService(IndexingService indexingService) {
        this.indexingService = indexingService;
    }
}
