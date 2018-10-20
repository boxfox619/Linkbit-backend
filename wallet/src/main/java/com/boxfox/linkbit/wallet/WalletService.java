package com.boxfox.linkbit.wallet;

import com.boxfox.cross.common.RoutingException;
import com.boxfox.linkbit.wallet.indexing.IndexingMessage;
import com.boxfox.linkbit.wallet.model.TransactionResult;
import com.boxfox.linkbit.wallet.model.WalletCreateResult;
import com.boxfox.linkbit.wallet.part.BalancePart;
import com.boxfox.linkbit.wallet.part.CreateWalletPart;
import com.boxfox.linkbit.wallet.part.TransactionPart;
import com.boxfox.vertx.service.AbstractService;
import com.google.gson.Gson;
import com.linkbit.android.entity.TransactionModel;

import java.util.List;

import static com.boxfox.linkbit.wallet.indexing.IndexingMessage.EVENT_SUBJECT;

public class WalletService extends AbstractService implements BalancePart, CreateWalletPart, TransactionPart {
    private WalletServiceContext context;

    public WalletService(WalletServiceContext context) {
        this.context = context;
    }

    @Override
    public double getBalance(String originalAddress) {
        return context.getBalancePart().getBalance(originalAddress);
    }

    @Override
    public WalletCreateResult createWallet(String password) {
        return context.getCreateWalletPart().createWallet(password);
    }

    @Override
    public TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount) {
        return context.getTransactionPart().send(walletFileName, walletJsonFile, password, targetAddress, amount);
    }

    @Override
    public List<TransactionModel> getTransactionList(String address) throws WalletServiceException, RoutingException {
        this.requestTransactionIndexing(address);
        return context.getTransactionPart().getTransactionList(address);
    }

    @Override
    public TransactionModel getTransaction(String transactionHash) throws WalletServiceException {
        return context.getTransactionPart().getTransaction(transactionHash);
    }

    @Override
    public int getTransactionCount(String address) throws WalletServiceException {
        return context.getTransactionPart().getTransactionCount(address);
    }

    @Override
    public void indexingTransaction(String address){
        context.getTransactionPart().indexingTransaction(address);
    }

    public void requestTransactionIndexing(String address) {
        IndexingMessage msg = new IndexingMessage();
        msg.setSymbol(context.symbol);
        msg.setAddress(address);
        getVertx().eventBus().publish(EVENT_SUBJECT, new Gson().toJson(msg));
    }
}
