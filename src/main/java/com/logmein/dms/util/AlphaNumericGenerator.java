package com.logmein.dms.util;

import java.util.Random;

/*
 * Reference from https://www.baeldung.com/java-random-string 
 */
public class AlphaNumericGenerator {

  private static final int LEFT_LIMIT = 48; // numeral '0'
  private static final int RIGHT_LIMIT = 122; // alphabet 'z'
  private static final int ALPHA_NUMERIC_ID_LENGTH = 20;

  public static String generateNextAlphaNumericId() {
    Random random = new Random();
    return random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(ALPHA_NUMERIC_ID_LENGTH)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
