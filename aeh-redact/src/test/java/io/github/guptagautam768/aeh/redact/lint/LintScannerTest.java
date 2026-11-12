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
package io.github.guptagautam768.aeh.redact.lint;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guptagautam768.aeh.ExitCodes;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LintScannerTest {

  @TempDir Path temp;

  @Test
  void plantedSecretFailsAndARedactedCassettePasses() throws Exception {
    Path planted = temp.resolve("notes.json");
    Files.writeString(planted, "{\"note\":\"ada@example.com\"}\n");
    LintReport dirty = new LintScanner().scan(List.of(planted));

    assertThat(dirty.exitCode()).isEqualTo(ExitCodes.INTEGRITY);
    assertThat(dirty.findings()).hasSize(1);
    assertThat(dirty.findings().get(0).ruleId()).isEqualTo("email");
    assertThat(dirty.findings().get(0).format()).doesNotContain("ada@example.com");

    Path cleanJson = temp.resolve("clean.json");
    Files.writeString(cleanJson, "{\"note\":\"<EMAIL_1>\",\"apiKey\":\"<SECRET_1>\"}\n");
    assertThat(new LintScanner().scan(List.of(cleanJson)).exitCode()).isEqualTo(ExitCodes.PASS);

    Path placeholder = temp.resolve("placeholder.txt");
    Files.writeString(placeholder, "contact <EMAIL_1> about <SECRET_2>\n");
    assertThat(new LintScanner().scan(List.of(placeholder)).exitCode()).isEqualTo(ExitCodes.PASS);
  }

  @Test
  void missingPathIsAUsageError() {
    LintReport report = new LintScanner().scan(List.of(temp.resolve("missing.json")));
    assertThat(report.exitCode()).isEqualTo(ExitCodes.USAGE);
    assertThat(report.missing()).hasSize(1);
  }
}
