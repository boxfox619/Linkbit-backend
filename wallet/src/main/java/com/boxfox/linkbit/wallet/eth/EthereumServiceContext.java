package com.boxfox.linkbit.wallet.eth;

import com.boxfox.linkbit.wallet.WalletServiceContext;
import com.boxfox.linkbit.wallet.part.BalancePart;
import com.boxfox.linkbit.wallet.part.CreateWalletPart;
import com.boxfox.linkbit.wallet.part.TransactionPart;
import com.boxfox.vertx.data.Config;

import java.io.File;

import io.vertx.core.Vertx;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class EthereumServiceContext extends WalletServiceContext {
    private EthereumBalancePartService balancePart;
    private EthereumCreateWalletPartService createWalletPart;
    private EthereumTransactionPartService transactionPart;

    private EthereumServiceContext() {
        super("ETH");
    }

    public static EthereumServiceContext create() {
        EthereumServiceContext service = new EthereumServiceContext();
        Web3j web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/326b0d7561824e0b8c4ee1f30e257019"));
        File cachePath = new File(Config.getDefaultInstance().getString("cachePath", "wallets"));
        if (!cachePath.exists())
            cachePath.mkdirs();
        service.balancePart = new EthereumBalancePartService(web3, cachePath);
        service.createWalletPart = new EthereumCreateWalletPartService(web3, cachePath);
        service.transactionPart = new EthereumTransactionPartService(web3, cachePath);
        return service;
    }

    @Override
    public BalancePart getBalancePart() {
        return this.balancePart;
    }

    @Override
    public CreateWalletPart getCreateWalletPart() {
        return this.createWalletPart;
    }

    @Override
    public TransactionPart getTransactionPart() {
        return this.transactionPart;
    }

    @Override
    protected boolean validAddress(String address) {
        return WalletUtils.isValidAddress(address);
    }

}
