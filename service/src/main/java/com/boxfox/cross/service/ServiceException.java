package com.boxfox.cross.service;

public class ServiceException extends Exception {

  private int code;

  public ServiceException(int code, String msg) {
    super(msg);
  }

  public ServiceException(int code) {
    super();
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }
}
