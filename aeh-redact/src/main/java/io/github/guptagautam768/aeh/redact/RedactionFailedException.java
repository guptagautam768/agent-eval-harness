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

import java.util.Map;

/** Fail-mode abort. The message contains counts only, never matched values (SEC-007). */
public final class RedactionFailedException extends IllegalStateException {

  private final Map<String, Integer> counts;

  public RedactionFailedException(Map<String, Integer> counts) {
    super("Redaction policy fail aborted the write; counts=" + counts);
    this.counts = Map.copyOf(counts);
  }

  public Map<String, Integer> counts() {
    return counts;
  }
}
