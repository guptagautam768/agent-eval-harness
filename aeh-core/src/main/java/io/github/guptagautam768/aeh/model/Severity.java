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

/**
 * Gate severity (ADR-0006). {@code MUST} fails the build; {@code SHOULD} warns; {@code INFO} is
 * reported.
 */
public enum Severity {
  MUST("MUST"),
  SHOULD("SHOULD"),
  INFO("INFO");

  private final String wire;

  Severity(String wire) {
    this.wire = wire;
  }

  @JsonValue
  public String wire() {
    return wire;
  }

  @JsonCreator
  public static Severity fromWire(String value) {
    for (Severity candidate : values()) {
      if (candidate.wire.equals(value)) {
        return candidate;
      }
    }
    throw new IllegalArgumentException("Unknown severity: " + value);
  }
}
