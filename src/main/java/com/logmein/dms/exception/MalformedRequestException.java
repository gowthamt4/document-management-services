package com.logmein.dms.exception;

public class MalformedRequestException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public MalformedRequestException(String message) {
    super(message);
  }

}
