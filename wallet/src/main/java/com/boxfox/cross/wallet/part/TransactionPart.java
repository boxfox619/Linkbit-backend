package com.boxfox.cross.wallet.part;

import com.boxfox.cross.wallet.WalletServiceException;
import com.boxfox.cross.wallet.model.TransactionResult;
import com.linkbit.android.entity.TransactionModel;
import io.vertx.core.Future;
import java.util.List;

public interface TransactionPart {

  TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount);

  Future<List<TransactionModel>> getTransactionList(String address) throws WalletServiceException;

  TransactionModel getTransaction(String transactionHash) throws WalletServiceException;

  int getTransactionCount(String address) throws WalletServiceException;
}
