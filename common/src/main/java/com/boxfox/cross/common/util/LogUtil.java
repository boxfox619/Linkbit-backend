package com.boxfox.cross.common.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LogUtil {
  public static Logger getLogger(){
    return LogManager.getRootLogger();
  }

}
