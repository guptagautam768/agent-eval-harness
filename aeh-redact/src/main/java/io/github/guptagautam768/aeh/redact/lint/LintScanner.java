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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.guptagautam768.aeh.json.AehJson;
import io.github.guptagautam768.aeh.redact.DefaultRedactor;
import io.github.guptagautam768.aeh.redact.RedactionHit;
import io.github.guptagautam768.aeh.redact.RedactionOutcome;
import io.github.guptagautam768.aeh.redact.RedactionPolicy;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Scans cassettes, baselines, judge cache and reports with the same rules as the recorder
 * (SEC-006). Placeholders are ignored because they do not match the detectors. JSON is parsed so
 * the entropy heuristic sees field names.
 */
public final class LintScanner {

  private static final long MAX_BYTES = 32L * 1024L * 1024L;
  private static final Set<String> SKIP_DIRECTORIES = Set.of(".git", ".gradle", "build", ".idea");

  private final DefaultRedactor redactor;
  private final AehJson json;

  public LintScanner() {
    this(DefaultRedactor.create(RedactionPolicy.defaults()), AehJson.shared());
  }

  public LintScanner(DefaultRedactor redactor) {
    this(redactor, AehJson.shared());
  }

  LintScanner(DefaultRedactor redactor, AehJson json) {
    this.redactor = redactor;
    this.json = json;
  }

  public LintReport scan(List<Path> paths) {
    List<LintFinding> findings = new ArrayList<>();
    List<String> missing = new ArrayList<>();
    for (Path path : paths) {
      if (!Files.exists(path)) {
        missing.add(path.toString());
        continue;
      }
      scanPath(path, findings);
    }
    findings.sort(Comparator.comparing(LintFinding::path).thenComparing(LintFinding::location));
    return new LintReport(findings, missing);
  }

  private void scanPath(Path path, List<LintFinding> findings) {
    if (Files.isDirectory(path)) {
      if (SKIP_DIRECTORIES.contains(path.getFileName().toString())) {
        return;
      }
      try (var stream = Files.list(path)) {
        stream.sorted().forEach(child -> scanPath(child, findings));
      } catch (IOException exception) {
        findings.add(new LintFinding(path.toString(), "io", "unreadable"));
      }
      return;
    }
    if (!Files.isRegularFile(path)) {
      return;
    }
    try {
      long size = Files.size(path);
      if (size > MAX_BYTES) {
        findings.add(new LintFinding(path.toString(), "size", "file_too_large"));
        return;
      }
      byte[] bytes = Files.readAllBytes(path);
      if (isBinary(bytes)) {
        return;
      }
      scanText(path, decode(bytes), findings);
    } catch (IOException exception) {
      findings.add(new LintFinding(path.toString(), "io", "unreadable"));
    }
  }

  private void scanText(Path path, String text, List<LintFinding> findings) {
    try {
      JsonNode tree = json.mapper().readTree(text);
      RedactionOutcome outcome = redactor.redactTree(tree, false);
      for (RedactionHit hit : outcome.hits()) {
        findings.add(new LintFinding(path.toString(), hit.location(), hit.ruleId()));
      }
    } catch (JsonProcessingException exception) {
      for (RedactionHit hit : redactor.scanText(text, path.toString())) {
        findings.add(new LintFinding(path.toString(), hit.location(), hit.ruleId()));
      }
    }
  }

  private static String decode(byte[] bytes) {
    try {
      return StandardCharsets.UTF_8
          .newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(java.nio.ByteBuffer.wrap(bytes))
          .toString();
    } catch (CharacterCodingException exception) {
      return new String(bytes, StandardCharsets.ISO_8859_1);
    }
  }

  private static boolean isBinary(byte[] bytes) {
    int limit = Math.min(bytes.length, 8000);
    for (int i = 0; i < limit; i++) {
      if (bytes[i] == 0) {
        return true;
      }
    }
    return false;
  }
}
