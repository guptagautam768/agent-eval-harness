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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class GateRulesTest {

  @Test
  void replayDefaultsMatchTheAcceptedTolerances() {
    Map<String, GateRule> rules =
        GateRules.defaults(RunMode.REPLAY).stream()
            .collect(Collectors.toMap(GateRule::metric, Function.identity()));

    assertRule(rules.get("pass_rate"), Direction.HIGHER, ToleranceKind.ABS, "0.02", Severity.MUST);
    assertRule(
        rules.get("tool_call_score"), Direction.HIGHER, ToleranceKind.ABS, "0.02", Severity.MUST);
    assertRule(rules.get("cost_usd"), Direction.LOWER, ToleranceKind.PCT, "10", Severity.MUST);
    assertRule(rules.get("tokens_total"), Direction.LOWER, ToleranceKind.PCT, "10", Severity.MUST);
    assertRule(
        rules.get("latency_ms_p95"), Direction.LOWER, ToleranceKind.PCT, "20", Severity.INFO);
    assertRule(
        rules.get("judge_score"), Direction.HIGHER, ToleranceKind.ABS, "0.05", Severity.SHOULD);
    assertRule(rules.get("steps"), Direction.LOWER, ToleranceKind.PCT, "10", Severity.MUST);
  }

  @Test
  void latencyIsMustOutsideReplay() {
    assertThat(rule(RunMode.RERUN).severity()).isEqualTo(Severity.MUST);
    assertThat(rule(RunMode.LIVE).severity()).isEqualTo(Severity.MUST);
    assertThat(rule(RunMode.RECORD).severity()).isEqualTo(Severity.INFO);
  }

  private static GateRule rule(RunMode mode) {
    return GateRules.defaults(mode).stream()
        .filter(candidate -> candidate.metric().equals("latency_ms_p95"))
        .findFirst()
        .orElseThrow();
  }

  private static void assertRule(
      GateRule rule, Direction direction, ToleranceKind kind, String value, Severity severity) {
    assertThat(rule.direction()).isEqualTo(direction);
    assertThat(rule.tolerance().kind()).isEqualTo(kind);
    assertThat(rule.tolerance().value()).isEqualByComparingTo(new BigDecimal(value));
    assertThat(rule.severity()).isEqualTo(severity);
  }
}
