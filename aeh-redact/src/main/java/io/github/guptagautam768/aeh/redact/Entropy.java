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
package io.github.guptagautam768.aeh.redact;

import java.util.HashMap;
import java.util.Map;

/**
 * Shannon entropy in bits per character. The secret heuristic uses {@code >= 4.0} (ADR-0009). A
 * tiny tolerance absorbs binary floating-point noise on values that are exactly 4.
 */
public final class Entropy {

  public static final double THRESHOLD = 4.0d;
  public static final int MIN_LENGTH = 32;

  private Entropy() {}

  public static double bitsPerChar(String value) {
    if (value == null || value.isEmpty()) {
      return 0d;
    }
    Map<Integer, Integer> counts = new HashMap<>();
    int length = 0;
    for (int i = 0; i < value.length(); ) {
      int point = value.codePointAt(i);
      counts.merge(point, 1, Integer::sum);
      length++;
      i += Character.charCount(point);
    }
    double entropy = 0d;
    for (int count : counts.values()) {
      double p = count / (double) length;
      entropy -= p * (Math.log(p) / Math.log(2));
    }
    return entropy;
  }

  public static boolean highEntropyToken(String value) {
    if (value == null || value.length() < MIN_LENGTH || hasWhitespace(value)) {
      return false;
    }
    return bitsPerChar(value) + 1e-9d >= THRESHOLD;
  }

  private static boolean hasWhitespace(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (Character.isWhitespace(value.charAt(i))) {
        return true;
      }
    }
    return false;
  }
}
