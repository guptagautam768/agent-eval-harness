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

import io.github.guptagautam768.aeh.json.Decimals;
import io.github.guptagautam768.aeh.model.CheckResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ScoreResult(List<CheckResult> checks, Map<String, BigDecimal> metrics) {

  public ScoreResult {
    checks = List.copyOf(checks == null ? List.of() : checks);
    metrics = Decimals.canonicalMap(metrics == null ? Map.of() : metrics);
  }
}
