package com.boxfox.cross.wallet.eth;

import com.boxfox.cross.wallet.part.BalancePart;
import io.vertx.core.Vertx;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

public class EthereumBalancePart extends EthereumPart implements BalancePart {

  public EthereumBalancePart(Vertx vertx, Web3j web3, File cachePath) {
    super(vertx, web3, cachePath);
  }

  @Override
  public double getBalance(String address) {
    try {
      EthGetBalance response = web3.ethGetBalance(address.replaceAll(" ",""), DefaultBlockParameterName.LATEST).send();
      String wei = response.getBalance().toString();
      BigDecimal bigDecimal = Convert.fromWei(wei, Convert.Unit.ETHER);
      return bigDecimal.doubleValue();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }
}
