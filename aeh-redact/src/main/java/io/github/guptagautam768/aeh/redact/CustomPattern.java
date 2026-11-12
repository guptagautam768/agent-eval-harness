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

import java.util.Objects;
import java.util.regex.Pattern;

/** A user-supplied regex rule (SEC-009). {@code placeholderType} becomes {@code <TYPE_n>}. */
public record CustomPattern(String id, Pattern pattern, String placeholderType) {

  public CustomPattern {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("custom pattern id is required");
    }
    Objects.requireNonNull(pattern, "pattern");
    if (placeholderType == null || !placeholderType.matches("[A-Z][A-Z0-9_]*")) {
      throw new IllegalArgumentException("placeholder type must match [A-Z][A-Z0-9_]*");
    }
  }
}
