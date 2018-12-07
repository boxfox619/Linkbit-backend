package com.boxfox.linkbit.wallet.eth.erc20;

import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

import com.boxfox.cross.common.entity.transaction.TransactionModel;
import com.boxfox.linkbit.util.DigitsUtils;
import com.boxfox.linkbit.util.ERC20Tokens;
import com.boxfox.linkbit.wallet.WalletServiceContext;
import com.boxfox.linkbit.wallet.WalletServiceException;
import com.boxfox.linkbit.wallet.model.TransactionResult;
import com.boxfox.linkbit.wallet.model.WalletCreateResult;
import com.boxfox.linkbit.wallet.part.BalancePart;
import com.boxfox.linkbit.wallet.part.CreateWalletPart;
import com.boxfox.linkbit.wallet.part.TransactionPart;
import com.boxfox.vertx.data.Config;
import com.google.common.io.Files;
import com.google.firebase.internal.NonNull;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;


public class ERC20ServiceContext extends WalletServiceContext implements TransactionPart, BalancePart, CreateWalletPart {
    protected static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
    protected static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    private Web3j web3;

    public ERC20ServiceContext(String symbol) {
        super(symbol);
        this.web3 = Web3j.build(new HttpService("https://mainnet.infura.io/JjSRoXryXbE6HgXJGILz"));
    }

    @Override
    public double getBalance(String address) {
        double balance = -1;
        String contractAddr = ERC20Tokens.getTokenAddress(symbol);
        String tknAddress = (address).substring(2);
        String contractData = ("0x70a08231000000000000000000000000" + tknAddress);
        try {
            String value = web3.ethCall(Transaction.createEthCallTransaction(address, contractAddr, contractData), LATEST).send().getValue();
            balance = Convert.fromWei(Numeric.toBigInt(value).toString(), Convert.Unit.ETHER).toBigInteger().doubleValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Double.valueOf(balance);
    }

    @Override
    public WalletCreateResult createWallet(String password) {
        return null;
    }

    @Override
    public TransactionResult send(String walletFileName, String walletJsonFile, String password, String targetAddress, String amount) {
        String cachePath = Config.getDefaultInstance().getString("cachePath", "cache");
        File tmpWallet = new File(cachePath+ File.separator + walletFileName);
        TransactionResult result = new TransactionResult();
        try {
            Files.write(walletJsonFile, tmpWallet, Charset.forName("UTF-8"));
            Credentials credentials = WalletUtils.loadCredentials(password, tmpWallet.getPath());
            String contractAddr = ERC20Tokens.getTokenAddress(symbol);
            String contractData = inputData("0xa9059cbb", targetAddress, amount);
            RawTransaction transaction = RawTransaction.createTransaction(new BigInteger("0x0"), GAS_PRICE, GAS_LIMIT, contractAddr, BigInteger.ZERO, contractData);
            byte[] signedMessage = TransactionEncoder.signMessage(transaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            String transactionHash = web3.ethSendRawTransaction(hexValue).send().getTransactionHash();
            result.setStatus(true);
            result.setTransactionHash(transactionHash);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String inputData(String methodIc, String address, String value) throws Exception {
        if (!WalletUtils.isValidAddress(address)) {
            throw new Exception("address error");
        }
        String strAddress = stringTo64Symbols(address);
        String strValue = stringValueFormat(value, 16);
        strValue = stringTo64Symbols(strValue);
        return methodIc + strAddress + strValue;
    }

    private static String stringValueFormat(String value, int radix) {
        BigDecimal bigDecimal = new BigDecimal(value);
        BigDecimal bd = new BigDecimal(DigitsUtils.divide);
        BigDecimal doubleWithStringValue = bd.multiply(bigDecimal);
        return doubleWithStringValue.toBigInteger().toString(radix);
    }

    @NonNull
    private static String stringTo64Symbols(String line) {
        if (line.charAt(0) == '0' && line.charAt(1) == 'x') {
            StringBuilder buffer = new StringBuilder(line);
            buffer.deleteCharAt(0);
            buffer.deleteCharAt(0);
            line = buffer.toString();
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append("0000000000000000000000000000000000000000000000000000000000000000");
        for (int i = 0; i < line.length(); i++) {
            buffer.setCharAt(64 - i - 1, line.charAt(line.length() - i - 1));
        }
        return buffer.toString();

    }

    @Override
    public List<TransactionModel> getTransactionList(String address) throws WalletServiceException {
        return null;
    }

    @Override
    public TransactionModel getTransaction(String transactionHash) throws WalletServiceException {
        return null;
    }

    @Override
    public int getTransactionCount(String address) throws WalletServiceException {
        return 0;
    }

    @Override
    public void indexingTransaction(String address) {

    }

    @Override
    public BalancePart getBalancePart() {
        return this;
    }

    @Override
    public CreateWalletPart getCreateWalletPart() {
        return this;
    }

    @Override
    public TransactionPart getTransactionPart() {
        return this;
    }
}
