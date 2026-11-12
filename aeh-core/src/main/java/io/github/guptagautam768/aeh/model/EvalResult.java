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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.github.guptagautam768.aeh.json.Decimals;
import io.github.guptagautam768.aeh.json.VersionedDocument;
import io.github.guptagautam768.aeh.schema.SchemaVersions;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Scored case (DM-006). */
@JsonPropertyOrder({"schemaVersion", "caseId", "runIds", "metrics", "checks", "judge"})
public record EvalResult(
    String schemaVersion,
    String caseId,
    List<String> runIds,
    Map<String, BigDecimal> metrics,
    List<CheckResult> checks,
    List<JudgeResult> judge)
    implements VersionedDocument {

  public EvalResult {
    SchemaVersions.requireSupported(schemaVersion, "result");
    Checks.caseId(caseId);
    runIds = Checks.list(runIds == null ? List.of() : runIds, "runIds");
    metrics = Decimals.canonicalMap(metrics == null ? Map.of() : metrics);
    checks = Checks.list(checks == null ? List.of() : checks, "checks");
    judge = Checks.list(judge == null ? List.of() : judge, "judge");
  }
}
