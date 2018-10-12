package com.boxfox.linkbit.wallet.eth;

import com.boxfox.linkbit.wallet.model.WalletCreateResult;
import com.boxfox.linkbit.wallet.part.CreateWalletPart;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.vertx.core.Vertx;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;

public class EthereumCreateWalletPartService extends EthereumPart implements CreateWalletPart {

  public EthereumCreateWalletPartService(Vertx vertx, Web3j web3, File cachePath) {
    super(vertx, web3, cachePath);
  }

  @Override
  public WalletCreateResult createWallet(String password) {
    WalletCreateResult result = new WalletCreateResult();
    try {
      String walletFileName = WalletUtils.generateFullNewWalletFile(password, cachePath);
      File jsonFile = new File(cachePath.getPath() + File.separator + walletFileName);
      String walletJson = Files.toString(jsonFile, Charset.defaultCharset());
      JsonObject walletJsonObj = (JsonObject) new JsonParser().parse(walletJson);
      String address = "0x"+walletJsonObj.get("address").getAsString();
      result.setResult(true);
      result.setAddress(address);
      result.setWalletName(walletFileName);
      result.setWalletData(walletJsonObj);
      jsonFile.delete();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
    } catch (InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    } catch (CipherException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
}
