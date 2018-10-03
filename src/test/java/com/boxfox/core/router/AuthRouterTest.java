package com.boxfox.core.router;


import org.junit.Test;

public class AuthRouterTest {

  @Test
  public void singinTest(){
    int firstNum = (int) (Math.random() * 9999 + 1);
    int secondNum = (int) (Math.random() * 999999 + 1);
    String address = String.format("linkbit-%04d-%06d", firstNum, secondNum);
    System.out.println(address);

  }
}
