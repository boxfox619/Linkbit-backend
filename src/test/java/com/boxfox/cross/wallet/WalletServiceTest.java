package com.boxfox.cross.wallet;

import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

public class WalletServiceTest {

  @Test
  public void test() throws IOException {
    String address = "0x1311edc0817c56c5bf66f151e9a4f61834774658";
    Web3j web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/326b0d7561824e0b8c4ee1f30e257019"));
    EthGetBalance response = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
    System.out.println(response.getResult());
    String wei = response.getBalance().toString();
    System.out.println(wei);
    BigDecimal bigDecimal = Convert.fromWei(wei, Convert.Unit.ETHER);
    System.out.println(bigDecimal);
  }

}
