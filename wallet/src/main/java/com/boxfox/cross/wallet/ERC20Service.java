package com.boxfox.cross.wallet;

import com.boxfox.cross.common.data.Config;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.service.wallet.WalletServiceException;
import com.boxfox.cross.service.wallet.model.TransactionResult;
import com.boxfox.cross.service.wallet.model.TransactionStatus;
import com.boxfox.cross.service.wallet.model.WalletCreateResult;
import com.google.common.io.Files;
import io.vertx.core.json.JsonArray;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;

import static com.boxfox.cross.service.network.RequestService.request;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;


public abstract class ERC20Service extends WalletService {

    protected static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
    protected static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    private static final String TOKEN_INFO_URL = "https://raw.githubusercontent.com/kvhnuke/etherwallet/v3.10.4.3/app/scripts/tokens/ethTokens.json";

    private Web3j web3;
    private File cachePath;
    private JsonArray tokens;

    public ERC20Service(String symbol){
        super(symbol);
        this.web3 = Web3j.build(new HttpService("https://mainnet.infura.io/JjSRoXryXbE6HgXJGILz"));
        this.cachePath = new File(Config.getDefaultInstance().getString("cachePath", "cache"));
        if (!cachePath.exists())
            cachePath.mkdirs();
        try {
            tokens = new JsonArray(request(TOKEN_INFO_URL));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getContractAddress(String symbol){
        for(int i = 0 ; i < tokens.size(); i++){
            if(tokens.getJsonObject(i).getString("symbol").equals(symbol.toUpperCase())){
                return tokens.getJsonObject(i).getString("address");
            }
        }
        return null;
    }

    @Override
    public String getBalance(String address) {
        String balance = null;
        String contractAddr = getContractAddress("EOS");
        String tknAddress = (address).substring(2);
        String contractData = ("0x70a08231000000000000000000000000" + tknAddress);
        try {
            String value = web3.ethCall(Transaction.createEthCallTransaction(address, contractAddr,contractData), LATEST).send().getValue();
            balance = Convert.fromWei(Numeric.toBigInt(value).toString(), Convert.Unit.ETHER).toBigInteger().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return balance;
    }


    @Override
    public WalletCreateResult createWallet(String password) {
        return null;
    }


    @Override
    public TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount) {
        File tmpWallet = new File(cachePath.getPath() + File.separator + walletFileName);
        TransactionResult result = new TransactionResult();
        try {
            Files.write(walletJsonFile, tmpWallet, Charset.forName("UTF-8"));
            Credentials credentials = WalletUtils.loadCredentials(password, tmpWallet.getPath());

            String contractAddr = getContractAddress("EOS");
            String tknTargetAddr = (targetAddress).substring(2);
            String contractData = ("0xa9059cbb000000000000000000000000"+tknTargetAddr);
            BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
            RawTransaction transaction = RawTransaction.createTransaction(new BigInteger("0x0"), GAS_PRICE, GAS_LIMIT, contractAddr, value, contractData);
            byte [] signedMessage = TransactionEncoder.signMessage(transaction, credentials);
            String hexValue = Hex.toHexString(signedMessage);
            String transactionHash = web3.ethSendRawTransaction(hexValue).send().getTransactionHash();
            result.setStatus(true);
            result.setTransactionHash(transactionHash);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<TransactionStatus> getTransactionList(String address) throws WalletServiceException {
        return null;
    }

    @Override
    public TransactionStatus getTransaction(String transactionHash) throws WalletServiceException {
        return null;
    }

    @Override
    public int getTransactionCount(String address) throws WalletServiceException {
        return 0;
    }
}