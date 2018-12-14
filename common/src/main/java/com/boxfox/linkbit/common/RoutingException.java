package com.boxfox.linkbit.common;

public class RoutingException extends Exception {

  private int code;

  public RoutingException(int code, String msg) {
    super(msg);
  }

  public RoutingException(int code) {
    super();
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }
}
