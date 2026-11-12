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
package io.github.guptagautam768.aeh.spi;

import io.github.guptagautam768.aeh.model.RunMode;
import java.util.Map;

/** Passed to scorers. Attributes stay open so later weeks can add judge and budget config. */
public record ScoringContext(RunMode mode, Map<String, String> attributes) {

  public ScoringContext {
    attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
  }
}
