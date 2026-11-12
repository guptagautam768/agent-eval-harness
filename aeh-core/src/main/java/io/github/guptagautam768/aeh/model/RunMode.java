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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Explicit harness mode (FR-007, ADR-0004). Default for CI is {@link #REPLAY}. */
public enum RunMode {
  RECORD("record"),
  REPLAY("replay"),
  RERUN("rerun"),
  LIVE("live");

  private final String wire;

  RunMode(String wire) {
    this.wire = wire;
  }

  @JsonValue
  public String wire() {
    return wire;
  }

  @JsonCreator
  public static RunMode fromWire(String value) {
    for (RunMode candidate : values()) {
      if (candidate.wire.equals(value)) {
        return candidate;
      }
    }
    throw new IllegalArgumentException("Unknown run mode: " + value);
  }
}
