package com.boxfox.linkbit.wallet.part;

import com.boxfox.linkbit.common.RoutingException;
import com.boxfox.linkbit.common.entity.transaction.TransactionModel;
import com.boxfox.linkbit.wallet.WalletServiceException;
import com.boxfox.linkbit.wallet.model.TransactionResult;
import java.util.List;

public interface TransactionPart {

    TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount);

    List<TransactionModel> getTransactionList(String address) throws WalletServiceException, RoutingException;

    TransactionModel getTransaction(String transactionHash) throws WalletServiceException;

    int getTransactionCount(String address) throws WalletServiceException;

    void indexingTransaction(String address);
}
