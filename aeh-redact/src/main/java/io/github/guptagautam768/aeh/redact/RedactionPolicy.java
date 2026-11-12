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
package io.github.guptagautam768.aeh.redact;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Record-time redaction settings (ADR-0009). The default redacts secrets and PII and continues. */
public record RedactionPolicy(
    String policyId,
    DetectionPolicy onDetect,
    boolean allowlist,
    Set<String> allowedHeaderNames,
    Set<String> allowedFieldNames,
    Set<String> extraDroppedHeaders,
    List<CustomPattern> customPatterns,
    boolean secretsEnabled,
    boolean piiEnabled,
    boolean loadServiceLoader) {

  public RedactionPolicy {
    if (policyId == null || policyId.isBlank()) {
      throw new IllegalArgumentException("policyId is required");
    }
    if (onDetect == null) {
      throw new IllegalArgumentException("onDetect is required");
    }
    allowedHeaderNames = lower(allowedHeaderNames);
    allowedFieldNames = allowedFieldNames == null ? Set.of() : Set.copyOf(allowedFieldNames);
    extraDroppedHeaders = lower(extraDroppedHeaders);
    customPatterns = List.copyOf(customPatterns == null ? List.of() : customPatterns);
  }

  public static RedactionPolicy defaults() {
    return new RedactionPolicy(
        "default",
        DetectionPolicy.REDACT,
        false,
        Set.of(),
        Set.of(),
        Set.of(),
        List.of(),
        true,
        true,
        true);
  }

  public RedactionPolicy withOnDetect(DetectionPolicy policy) {
    return new RedactionPolicy(
        policyId,
        policy,
        allowlist,
        allowedHeaderNames,
        allowedFieldNames,
        extraDroppedHeaders,
        customPatterns,
        secretsEnabled,
        piiEnabled,
        loadServiceLoader);
  }

  public RedactionPolicy withAllowlist(Set<String> headers, Set<String> fields) {
    return new RedactionPolicy(
        policyId,
        onDetect,
        true,
        headers,
        fields,
        extraDroppedHeaders,
        customPatterns,
        secretsEnabled,
        piiEnabled,
        loadServiceLoader);
  }

  private static Set<String> lower(Set<String> values) {
    if (values == null || values.isEmpty()) {
      return Set.of();
    }
    return values.stream()
        .map(value -> value.toLowerCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableSet());
  }
}
