/*
 * Copyright 2026 Gautam Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.guptagautam768.aeh.redact.checksum;

/** Luhn, Verhoeff and IBAN mod-97 checks used by the PII detectors (ADR-0009). */
public final class Checksums {

  private static final int[][] VERHOEFF_D = {
    {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
    {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
    {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
    {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
    {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
    {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
    {6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
    {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
    {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
    {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}
  };

  private static final int[][] VERHOEFF_P = {
    {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
    {1, 5, 7, 6, 2, 8, 3, 0, 9, 4},
    {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
    {8, 9, 1, 6, 0, 4, 3, 5, 2, 7},
    {9, 4, 5, 3, 1, 2, 6, 8, 7, 0},
    {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
    {2, 7, 9, 3, 8, 0, 6, 4, 1, 5},
    {7, 0, 4, 6, 9, 1, 3, 2, 5, 8}
  };

  private static final int[] VERHOEFF_INV = {0, 4, 3, 2, 1, 5, 6, 7, 8, 9};

  private Checksums() {}

  public static boolean luhn(String digits) {
    if (digits == null || digits.length() < 13 || digits.length() > 19) {
      return false;
    }
    int sum = 0;
    boolean doubleIt = false;
    for (int i = digits.length() - 1; i >= 0; i--) {
      char c = digits.charAt(i);
      if (c < '0' || c > '9') {
        return false;
      }
      int n = c - '0';
      if (doubleIt) {
        n *= 2;
        if (n > 9) {
          n -= 9;
        }
      }
      sum += n;
      doubleIt = !doubleIt;
    }
    return sum % 10 == 0;
  }

  public static boolean verhoeff(String digits) {
    if (digits == null || digits.isEmpty()) {
      return false;
    }
    int checksum = 0;
    int length = digits.length();
    for (int i = 0; i < length; i++) {
      char c = digits.charAt(length - 1 - i);
      if (c < '0' || c > '9') {
        return false;
      }
      checksum = VERHOEFF_D[checksum][VERHOEFF_P[i % 8][c - '0']];
    }
    return checksum == 0;
  }

  public static char verhoeffCheckDigit(String payload) {
    int checksum = 0;
    int length = payload.length();
    for (int i = 0; i < length; i++) {
      int digit = payload.charAt(length - 1 - i) - '0';
      checksum = VERHOEFF_D[checksum][VERHOEFF_P[(i + 1) % 8][digit]];
    }
    return (char) ('0' + VERHOEFF_INV[checksum]);
  }

  public static boolean iban(String compact) {
    if (compact == null) {
      return false;
    }
    int length = compact.length();
    if (length < 15 || length > 34) {
      return false;
    }
    String moved = compact.substring(4) + compact.substring(0, 4);
    int mod = 0;
    for (int i = 0; i < moved.length(); i++) {
      char c = moved.charAt(i);
      if (c >= '0' && c <= '9') {
        mod = (mod * 10 + (c - '0')) % 97;
      } else if (c >= 'A' && c <= 'Z') {
        int value = c - 'A' + 10;
        mod = (mod * 10 + (value / 10)) % 97;
        mod = (mod * 10 + (value % 10)) % 97;
      } else {
        return false;
      }
    }
    return mod == 1;
  }
}
