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
package io.github.guptagautam768.aeh.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

final class Checks {

  private static final Pattern CASE_ID = Pattern.compile("[a-z0-9._-]+");

  private Checks() {}

  static String required(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " is required");
    }
    return value;
  }

  static void caseId(String caseId) {
    if (caseId == null
        || !CASE_ID.matcher(caseId).matches()
        || ".".equals(caseId)
        || "..".equals(caseId)) {
      throw new IllegalArgumentException(
          "caseId must match [a-z0-9._-] and must not be '.' or '..'");
    }
  }

  static <T> List<T> list(List<T> values, String name) {
    List<T> copy = new ArrayList<>(Objects.requireNonNull(values, name));
    if (copy.contains(null)) {
      throw new IllegalArgumentException(name + " must not contain null");
    }
    return List.copyOf(copy);
  }

  static Map<String, String> strings(Map<String, String> values, String name) {
    Objects.requireNonNull(values, name);
    Map<String, String> copy = new LinkedHashMap<>();
    values.forEach(
        (key, value) -> {
          if (key == null || key.isBlank() || value == null) {
            throw new IllegalArgumentException(name + " entries must be non-null");
          }
          copy.put(key, value);
        });
    return Map.copyOf(copy);
  }
}
