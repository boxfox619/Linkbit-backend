package com.boxfox.linkbit.wallet.part;

import com.boxfox.linkbit.wallet.WalletServiceException;
import com.boxfox.linkbit.wallet.model.TransactionResult;
import com.linkbit.android.entity.TransactionModel;
import io.vertx.core.Future;
import java.util.List;

public interface TransactionPart {

  TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount);

  Future<List<TransactionModel>> getTransactionList(String address) throws WalletServiceException;

  TransactionModel getTransaction(String transactionHash) throws WalletServiceException;

  int getTransactionCount(String address) throws WalletServiceException;
}
