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
package io.github.guptagautam768.aeh.cli;

import io.github.guptagautam768.aeh.redact.lint.LintFinding;
import io.github.guptagautam768.aeh.redact.lint.LintReport;
import io.github.guptagautam768.aeh.redact.lint.LintScanner;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "lint",
    description = "Scan files for secrets and PII. Exits 3 on a hit.")
public final class LintCommand implements Callable<Integer> {

  @CommandLine.Parameters(
      arity = "1..*",
      paramLabel = "PATH",
      description = "Files or directories to scan")
  List<Path> paths;

  @Override
  public Integer call() {
    LintReport report = new LintScanner().scan(paths);
    for (String missing : report.missing()) {
      System.err.println("lint: not found: " + missing);
    }
    for (LintFinding finding : report.findings()) {
      System.err.println(finding.format());
    }
    if (report.findings().isEmpty() && report.missing().isEmpty()) {
      System.out.println("lint: no secrets or PII detected");
    }
    return report.exitCode();
  }
}
