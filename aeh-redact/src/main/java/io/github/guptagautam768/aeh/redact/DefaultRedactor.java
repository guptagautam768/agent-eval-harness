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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.guptagautam768.aeh.json.AehJson;
import io.github.guptagautam768.aeh.model.Cassette;
import io.github.guptagautam768.aeh.model.RedactionRecord;
import io.github.guptagautam768.aeh.redact.spi.SpanDetector;
import io.github.guptagautam768.aeh.redact.spi.TextSpan;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-memory redactor (ADR-0009). Object keys are visited in sorted order so placeholder numbers are
 * stable (see proposed ADR-0014). The value-to-placeholder map is discarded when the call returns.
 */
public final class DefaultRedactor {

  private static final Logger log = LoggerFactory.getLogger(DefaultRedactor.class);
  private static final Pattern KEY_LIKE = Pattern.compile("(?i)(key|token|secret|password|auth)");
  private static final Pattern HEADER_OBJECT = Pattern.compile("(?i)^headers?$");
  private static final Set<String> DROPPED_HEADERS =
      Set.of(
          "authorization", "proxy-authorization", "x-api-key", "api-key", "cookie", "set-cookie");

  private final RedactionPolicy policy;
  private final List<SpanDetector> detectors;
  private final List<String> rulesApplied;
  private final AehJson json;

  public DefaultRedactor(RedactionPolicy policy, List<SpanDetector> detectors) {
    this(policy, detectors, AehJson.shared());
  }

  DefaultRedactor(RedactionPolicy policy, List<SpanDetector> detectors, AehJson json) {
    this.policy = policy;
    this.detectors = List.copyOf(detectors);
    this.json = json;
    TreeSet<String> rules = new TreeSet<>();
    for (SpanDetector detector : this.detectors) {
      rules.add(detector.ruleId());
    }
    if (policy.secretsEnabled()) {
      rules.add("entropy");
      rules.add("dropped_header");
    }
    if (policy.allowlist()) {
      rules.add("allowlist");
    }
    this.rulesApplied = List.copyOf(rules);
  }

  public static DefaultRedactor create(RedactionPolicy policy) {
    List<SpanDetector> detectors = new ArrayList<>();
    if (policy.secretsEnabled()) {
      detectors.addAll(Detectors.secrets());
    }
    if (policy.piiEnabled()) {
      detectors.addAll(Detectors.pii());
    }
    for (CustomPattern pattern : policy.customPatterns()) {
      detectors.add(Detectors.custom(pattern));
    }
    if (policy.loadServiceLoader()) {
      ServiceLoader.load(SpanDetector.class).forEach(detectors::add);
    }
    return new DefaultRedactor(policy, detectors);
  }

  public Cassette redact(Cassette cassette) {
    JsonNode tree = json.mapper().valueToTree(cassette);
    RedactionOutcome outcome = redactTree(tree, true);
    if (policy.onDetect() == DetectionPolicy.FAIL && !outcome.hits().isEmpty()) {
      log.error("Redaction policy fail aborted the write; counts={}", outcome.record().counts());
      throw new RedactionFailedException(outcome.record().counts());
    }
    ObjectNode object = (ObjectNode) outcome.tree();
    object.set("redaction", json.mapper().valueToTree(outcome.record()));
    return json.mapper().convertValue(object, Cassette.class);
  }

  public RedactionOutcome redactTree(JsonNode node) {
    return redactTree(node, true);
  }

  public RedactionOutcome redactTree(JsonNode node, boolean logCounts) {
    Session session = new Session();
    JsonNode redacted = walk(node, "", null, false, session);
    RedactionRecord record = new RedactionRecord(policy.policyId(), rulesApplied, session.counts);
    if (logCounts && !session.counts.isEmpty()) {
      log.warn("Redacted sensitive values before write: {}", session.counts);
    }
    return new RedactionOutcome(redacted, record, session.hits);
  }

  /** Pattern scan of raw text. Entropy is not applied, because there are no field names. */
  public List<RedactionHit> scanText(String text, String path) {
    Session session = new Session();
    String[] lines = text.split("\\R", -1);
    for (int i = 0; i < lines.length; i++) {
      List<Match> matches = match(lines[i], false);
      for (Match match : matches) {
        session.count(match.ruleId);
        session.hits.add(new RedactionHit(path + ":" + (i + 1), match.ruleId));
      }
    }
    return List.copyOf(session.hits);
  }

  private JsonNode walk(
      JsonNode node, String pointer, String fieldName, boolean inHeader, Session session) {
    if (node == null || node.isNull()) {
      return node;
    }
    if (node.isObject()) {
      ObjectNode out = json.mapper().createObjectNode();
      List<String> names = new ArrayList<>();
      node.fieldNames().forEachRemaining(names::add);
      names.sort(Comparator.naturalOrder());
      for (String name : names) {
        String childPointer = pointer + "/" + escape(name);
        JsonNode child = node.get(name);
        if (policy.secretsEnabled() && isDroppedHeaderName(name) && !child.isContainerNode()) {
          session.count("dropped_header");
          session.hits.add(new RedactionHit(childPointer, "dropped_header"));
          continue;
        }
        if (child.isObject() && HEADER_OBJECT.matcher(name).matches()) {
          out.set(name, walkHeaders(child, childPointer, session));
          continue;
        }
        if (policy.allowlist() && !policy.allowedFieldNames().contains(name)) {
          session.count("allowlist");
          session.hits.add(new RedactionHit(childPointer, "allowlist"));
          continue;
        }
        out.set(name, walk(child, childPointer, name, false, session));
      }
      return out;
    }
    if (node.isArray()) {
      ArrayNode out = json.mapper().createArrayNode();
      for (int i = 0; i < node.size(); i++) {
        out.add(walk(node.get(i), pointer + "/" + i, fieldName, inHeader, session));
      }
      return out;
    }
    if (node.isTextual() || node.isNumber()) {
      String raw = node.asText();
      String redacted = replace(raw, fieldName, inHeader, pointer, session);
      if (redacted.equals(raw)) {
        return node;
      }
      return TextNode.valueOf(redacted);
    }
    return node;
  }

  private ObjectNode walkHeaders(JsonNode headers, String pointer, Session session) {
    ObjectNode out = json.mapper().createObjectNode();
    List<String> names = new ArrayList<>();
    headers.fieldNames().forEachRemaining(names::add);
    names.sort(String.CASE_INSENSITIVE_ORDER);
    for (String name : names) {
      String childPointer = pointer + "/" + escape(name);
      boolean dropped = policy.secretsEnabled() && isDroppedHeaderName(name);
      boolean hidden =
          policy.allowlist()
              && !policy.allowedHeaderNames().contains(name.toLowerCase(Locale.ROOT));
      if (dropped || hidden) {
        String rule = dropped ? "dropped_header" : "allowlist";
        session.count(rule);
        session.hits.add(new RedactionHit(childPointer, rule));
        continue;
      }
      out.set(name, walk(headers.get(name), childPointer, name, true, session));
    }
    return out;
  }

  private boolean isDroppedHeaderName(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    return DROPPED_HEADERS.contains(lower) || policy.extraDroppedHeaders().contains(lower);
  }

  private String replace(
      String raw, String fieldName, boolean inHeader, String pointer, Session session) {
    List<Match> matches = match(raw, inHeader || keyLike(fieldName));
    if (matches.isEmpty()) {
      return raw;
    }
    StringBuilder builder = new StringBuilder(raw.length());
    int cursor = 0;
    for (Match match : matches) {
      builder.append(raw, cursor, match.start);
      String original = raw.substring(match.start, match.end);
      builder.append(session.placeholder(match.placeholderType, original));
      session.count(match.ruleId);
      session.hits.add(new RedactionHit(pointer, match.ruleId));
      cursor = match.end;
    }
    builder.append(raw, cursor, raw.length());
    return builder.toString();
  }

  private List<Match> match(String text, boolean entropy) {
    List<Match> found = new ArrayList<>();
    for (SpanDetector detector : detectors) {
      for (TextSpan span : detector.detect(text)) {
        found.add(
            new Match(
                span.start(),
                span.end(),
                detector.ruleId(),
                detector.placeholderType(),
                detector.priority()));
      }
    }
    if (entropy) {
      found.addAll(entropyMatches(text));
    }
    return resolve(found);
  }

  private static List<Match> entropyMatches(String text) {
    List<Match> matches = new ArrayList<>();
    int i = 0;
    while (i < text.length()) {
      while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
        i++;
      }
      int start = i;
      while (i < text.length() && !Character.isWhitespace(text.charAt(i))) {
        i++;
      }
      if (i > start && Entropy.highEntropyToken(text.substring(start, i))) {
        matches.add(new Match(start, i, "entropy", "SECRET", 40));
      }
    }
    return matches;
  }

  private static boolean keyLike(String fieldName) {
    return fieldName != null && KEY_LIKE.matcher(fieldName).find();
  }

  private static List<Match> resolve(List<Match> matches) {
    List<Match> ordered = new ArrayList<>(matches);
    ordered.sort(
        Comparator.comparingInt(Match::length)
            .reversed()
            .thenComparingInt(match -> match.priority)
            .thenComparingInt(match -> match.start));
    List<Match> kept = new ArrayList<>();
    for (Match match : ordered) {
      boolean overlaps = false;
      for (Match existing : kept) {
        if (match.start < existing.end && existing.start < match.end) {
          overlaps = true;
          break;
        }
      }
      if (!overlaps) {
        kept.add(match);
      }
    }
    kept.sort(Comparator.comparingInt(match -> match.start));
    return kept;
  }

  private static String escape(String field) {
    return field.replace("~", "~0").replace("/", "~1");
  }

  private static final class Session {
    private final Map<String, Map<String, Integer>> assigned = new LinkedHashMap<>();
    private final Map<String, Integer> next = new LinkedHashMap<>();
    private final Map<String, Integer> counts = new LinkedHashMap<>();
    private final List<RedactionHit> hits = new ArrayList<>();

    String placeholder(String type, String original) {
      Map<String, Integer> byValue =
          assigned.computeIfAbsent(type, ignored -> new LinkedHashMap<>());
      Integer existing = byValue.get(original);
      if (existing != null) {
        return format(type, existing);
      }
      int n = next.merge(type, 1, Integer::sum);
      byValue.put(original, n);
      return format(type, n);
    }

    void count(String ruleId) {
      counts.merge(ruleId, 1, Integer::sum);
    }

    private static String format(String type, int n) {
      return "<" + type + "_" + n + ">";
    }
  }

  private static final class Match {
    private final int start;
    private final int end;
    private final String ruleId;
    private final String placeholderType;
    private final int priority;

    private Match(int start, int end, String ruleId, String placeholderType, int priority) {
      this.start = start;
      this.end = end;
      this.ruleId = ruleId;
      this.placeholderType = placeholderType;
      this.priority = priority;
    }

    private int length() {
      return end - start;
    }
  }
}
