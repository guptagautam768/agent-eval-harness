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
package io.github.guptagautam768.aeh.redact.spi;

import java.util.List;

/** Test-only detector, loaded through ServiceLoader. */
public final class MarkerDetector implements SpanDetector {

  static final String MARKER = "SYNTHETIC_MARKER";

  @Override
  public String ruleId() {
    return "marker";
  }

  @Override
  public String placeholderType() {
    return "SECRET";
  }

  @Override
  public int priority() {
    return 5;
  }

  @Override
  public List<TextSpan> detect(String text) {
    int index = text.indexOf(MARKER);
    if (index < 0) {
      return List.of();
    }
    return List.of(new TextSpan(index, index + MARKER.length()));
  }
}
