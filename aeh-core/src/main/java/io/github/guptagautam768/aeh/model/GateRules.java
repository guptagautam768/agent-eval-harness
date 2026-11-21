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

import java.math.BigDecimal;
import java.util.List;

/**
 * Default gate rules from ADR-0008 / RD-013. Tolerances are positive magnitudes; direction says
 * which way is a regression. {@code accuracy_deterministic} is MUST in ADR-0006 but has no numeric
 * tolerance in RD-013, so it is not in this list until one is chosen.
 */
public final class GateRules {

  private GateRules() {}

  public static List<GateRule> defaults(RunMode mode) {
    Severity latency =
        mode == RunMode.RERUN || mode == RunMode.LIVE ? Severity.MUST : Severity.INFO;
    return List.of(
        rule("pass_rate", Direction.HIGHER, ToleranceKind.ABS, "0.02", Severity.MUST),
        rule("tool_call_score", Direction.HIGHER, ToleranceKind.ABS, "0.02", Severity.MUST),
        rule("cost_usd", Direction.LOWER, ToleranceKind.PCT, "10", Severity.MUST),
        rule("tokens_total", Direction.LOWER, ToleranceKind.PCT, "10", Severity.MUST),
        rule("latency_ms_p95", Direction.LOWER, ToleranceKind.PCT, "20", latency),
        rule("judge_score", Direction.HIGHER, ToleranceKind.ABS, "0.05", Severity.SHOULD),
        rule("steps", Direction.LOWER, ToleranceKind.PCT, "10", Severity.MUST));
  }

  private static GateRule rule(
      String metric, Direction direction, ToleranceKind kind, String value, Severity severity) {
    return new GateRule(
        metric, direction, null, null, new Tolerance(kind, new BigDecimal(value)), severity);
  }
}
