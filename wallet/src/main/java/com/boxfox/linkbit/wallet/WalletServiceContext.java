package com.boxfox.linkbit.wallet;

import com.boxfox.linkbit.wallet.indexing.IndexingService;
import com.boxfox.linkbit.wallet.part.BalancePart;
import com.boxfox.linkbit.wallet.part.CreateWalletPart;
import com.boxfox.linkbit.wallet.part.TransactionPart;

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
        return password -> null;
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
