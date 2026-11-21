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

import io.github.guptagautam768.aeh.redact.checksum.Checksums;
import io.github.guptagautam768.aeh.redact.spi.SpanDetector;
import io.github.guptagautam768.aeh.redact.spi.TextSpan;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Built-in secret and PII detectors. Categories match ADR-0009. */
public final class Detectors {

  private Detectors() {}

  public static List<SpanDetector> secrets() {
    return List.of(
        new RegexDetector("pem", "SECRET", 10, Pem.BLOCK, false),
        new RegexDetector(
            "anthropic_key", "SECRET", 11, Pattern.compile("sk-ant-[A-Za-z0-9_\\-]{16,}"), false),
        new RegexDetector(
            "openai_key", "SECRET", 12, Pattern.compile("sk-(?!ant-)[A-Za-z0-9_\\-]{20,}"), false),
        new AwsDetector(),
        new AwsSecretDetector(),
        new RegexDetector(
            "github_token",
            "SECRET",
            15,
            Pattern.compile("(?:gh[pos]_[A-Za-z0-9]{36}|github_pat_[A-Za-z0-9_]{22,})"),
            false),
        new RegexDetector(
            "jwt",
            "SECRET",
            16,
            Pattern.compile("eyJ[A-Za-z0-9_\\-]{8,}\\.[A-Za-z0-9_\\-]{8,}\\.[A-Za-z0-9_\\-]{8,}"),
            false),
        new UrlCredentialDetector());
  }

  public static List<SpanDetector> pii() {
    return List.of(
        new CardDetector(),
        new AadhaarDetector(),
        new IbanDetector(),
        new RegexDetector(
            "pan",
            "PAN",
            130,
            Pattern.compile("(?i)(?<![A-Z0-9])[A-Z]{3}[ABCFGHLJPT][A-Z]\\d{4}[A-Z](?![A-Z0-9])"),
            false),
        new RegexDetector(
            "ipv6",
            "IPV6",
            140,
            Pattern.compile("(?i)(?<![0-9A-Z:])(?:[0-9A-F]{0,4}:){2,7}[0-9A-F]{0,4}(?![0-9A-Z:])"),
            false) {
          @Override
          public List<TextSpan> detect(String text) {
            List<TextSpan> spans = new ArrayList<>();
            for (TextSpan span : super.detect(text)) {
              if (Ipv6.valid(text.substring(span.start(), span.end()))) {
                spans.add(span);
              }
            }
            return spans;
          }
        },
        new RegexDetector(
            "ipv4",
            "IPV4",
            150,
            Pattern.compile(
                "(?<!\\d)(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?!\\d)"),
            false),
        new PhoneDetector(),
        new RegexDetector(
            "email",
            "EMAIL",
            170,
            Pattern.compile(
                "(?i)(?<![A-Z0-9._%+\\-])[A-Z0-9._%+\\-]+@[A-Z0-9](?:[A-Z0-9\\-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9\\-]{0,61}[A-Z0-9])?)*\\.[A-Z]{2,}(?![A-Z0-9])"),
            false));
  }

  public static SpanDetector custom(CustomPattern pattern) {
    return new RegexDetector(
        pattern.id(), pattern.placeholderType(), 200, pattern.pattern(), false);
  }

  static class RegexDetector implements SpanDetector {
    private final String ruleId;
    private final String placeholderType;
    private final int priority;
    private final Pattern pattern;
    private final boolean unused;

    RegexDetector(
        String ruleId, String placeholderType, int priority, Pattern pattern, boolean unused) {
      this.ruleId = ruleId;
      this.placeholderType = placeholderType;
      this.priority = priority;
      this.pattern = pattern;
      this.unused = unused;
    }

    @Override
    public String ruleId() {
      return ruleId;
    }

    @Override
    public String placeholderType() {
      return placeholderType;
    }

    @Override
    public int priority() {
      return priority;
    }

    @Override
    public List<TextSpan> detect(String text) {
      if (text == null || text.isEmpty()) {
        return List.of();
      }
      List<TextSpan> spans = new ArrayList<>();
      Matcher matcher = pattern.matcher(text);
      while (matcher.find()) {
        spans.add(new TextSpan(matcher.start(), matcher.end()));
      }
      return spans;
    }
  }

  static final class AwsDetector implements SpanDetector {
    static final Pattern ACCESS = Pattern.compile("(?<![A-Z0-9])AKIA[0-9A-Z]{16}(?![A-Z0-9])");
    static final Pattern SECRET =
        Pattern.compile("(?<![A-Za-z0-9/+=])[A-Za-z0-9/+]{40}(?![A-Za-z0-9/+=])");

    @Override
    public String ruleId() {
      return "aws_access_key";
    }

    @Override
    public String placeholderType() {
      return "SECRET";
    }

    @Override
    public int priority() {
      return 13;
    }

    @Override
    public List<TextSpan> detect(String text) {
      if (text == null || text.isEmpty()) {
        return List.of();
      }
      List<TextSpan> spans = new ArrayList<>();
      Matcher access = ACCESS.matcher(text);
      while (access.find()) {
        spans.add(new TextSpan(access.start(), access.end()));
      }
      return spans;
    }
  }

  static final class AwsSecretDetector implements SpanDetector {
    @Override
    public String ruleId() {
      return "aws_secret";
    }

    @Override
    public String placeholderType() {
      return "SECRET";
    }

    @Override
    public int priority() {
      return 14;
    }

    @Override
    public List<TextSpan> detect(String text) {
      if (text == null || text.isEmpty()) {
        return List.of();
      }
      List<TextSpan> spans = new ArrayList<>();
      Matcher access = AwsDetector.ACCESS.matcher(text);
      while (access.find()) {
        int windowEnd = Math.min(text.length(), access.end() + 200);
        Matcher secret = AwsDetector.SECRET.matcher(text).region(access.end(), windowEnd);
        if (secret.find()) {
          spans.add(new TextSpan(secret.start(), secret.end()));
        }
      }
      return spans;
    }
  }

  static final class UrlCredentialDetector implements SpanDetector {
    private static final Pattern URL = Pattern.compile("(?i)https?://([^\\s/@:]+):([^\\s/@]+)@");

    @Override
    public String ruleId() {
      return "url_credential";
    }

    @Override
    public String placeholderType() {
      return "SECRET";
    }

    @Override
    public int priority() {
      return 17;
    }

    @Override
    public List<TextSpan> detect(String text) {
      if (text == null || text.isEmpty()) {
        return List.of();
      }
      List<TextSpan> spans = new ArrayList<>();
      Matcher matcher = URL.matcher(text);
      while (matcher.find()) {
        spans.add(new TextSpan(matcher.start(1), matcher.end(2)));
      }
      return spans;
    }
  }

  static final class CardDetector implements SpanDetector {
    private static final Pattern CANDIDATE =
        Pattern.compile("(?<!\\d)(?:\\d[ \\t-]?){12,18}\\d(?!\\d)");

    @Override
    public String ruleId() {
      return "credit_card";
    }

    @Override
    public String placeholderType() {
      return "CREDIT_CARD";
    }

    @Override
    public int priority() {
      return 100;
    }

    @Override
    public List<TextSpan> detect(String text) {
      if (text == null || text.isEmpty()) {
        return List.of();
      }
      List<TextSpan> spans = new ArrayList<>();
      Matcher matcher = CANDIDATE.matcher(text);
      while (matcher.find()) {
        String raw = matcher.group();
        if (singleSeparators(raw) && Checksums.luhn(digits(raw))) {
          spans.add(new TextSpan(matcher.start(), matcher.end()));
        }
      }
      return spans;
    }
  }

  static final class AadhaarDetector implements SpanDetector {
    private static final Pattern CANDIDATE =
        Pattern.compile("(?<!\\d)[2-9]\\d{3}[ \\t]?\\d{4}[ \\t]?\\d{4}(?!\\d)");

    @Override
    public String ruleId() {
      return "aadhaar";
    }

    @Override
    public String placeholderType() {
      return "AADHAAR";
    }

    @Override
    public int priority() {
      return 110;
    }

    @Override
    public List<TextSpan> detect(String text) {
      if (text == null || text.isEmpty()) {
        return List.of();
      }
      List<TextSpan> spans = new ArrayList<>();
      Matcher matcher = CANDIDATE.matcher(text);
      while (matcher.find()) {
        String rawDigits = digits(matcher.group());
        if (rawDigits.length() == 12 && Checksums.verhoeff(rawDigits)) {
          spans.add(new TextSpan(matcher.start(), matcher.end()));
        }
      }
      return spans;
    }
  }

  static final class IbanDetector implements SpanDetector {
    private static final Pattern COMPACT =
        Pattern.compile("(?i)(?<![A-Z0-9])[A-Z]{2}\\d{2}[A-Z0-9]{11,30}(?![A-Z0-9])");
    private static final Pattern SPACED =
        Pattern.compile(
            "(?i)(?<![A-Z0-9])[A-Z]{2}\\d{2}(?:[ \\t][A-Z0-9]{4})+[ \\t][A-Z0-9]{1,4}(?![A-Z0-9])");

    @Override
    public String ruleId() {
      return "iban";
    }

    @Override
    public String placeholderType() {
      return "IBAN";
    }

    @Override
    public int priority() {
      return 120;
    }

    @Override
    public List<TextSpan> detect(String text) {
      if (text == null || text.isEmpty()) {
        return List.of();
      }
      List<TextSpan> spans = new ArrayList<>();
      collect(text, COMPACT, spans);
      collect(text, SPACED, spans);
      return spans;
    }

    private static void collect(String text, Pattern pattern, List<TextSpan> spans) {
      Matcher matcher = pattern.matcher(text);
      while (matcher.find()) {
        if (overlaps(spans, matcher.start(), matcher.end())) {
          continue;
        }
        if (Checksums.iban(compact(matcher.group()))) {
          spans.add(new TextSpan(matcher.start(), matcher.end()));
        }
      }
    }
  }

  static final class PhoneDetector implements SpanDetector {
    private static final Pattern MOBILE = Pattern.compile("(?<!\\d)0?[6-9]\\d{9}(?!\\d)");
    private static final Pattern MOBILE_GROUPED =
        Pattern.compile("(?<!\\d)[6-9]\\d{4}[ \\t-]\\d{5}(?!\\d)");
    private static final Pattern LANDLINE =
        Pattern.compile("(?<!\\d)0[1-9]\\d{1,3}[ \\t-]\\d{6,8}(?!\\d)");
    private static final Pattern LANDLINE_COMPACT = Pattern.compile("(?<!\\d)0[1-9]\\d{9}(?!\\d)");

    @Override
    public String ruleId() {
      return "phone";
    }

    @Override
    public String placeholderType() {
      return "PHONE";
    }

    @Override
    public int priority() {
      return 160;
    }

    @Override
    public List<TextSpan> detect(String text) {
      if (text == null || text.isEmpty()) {
        return List.of();
      }
      List<TextSpan> spans = new ArrayList<>();
      scanInternational(text, spans);
      collect(MOBILE.matcher(text), spans);
      collect(MOBILE_GROUPED.matcher(text), spans);
      Matcher landline = LANDLINE.matcher(text);
      while (landline.find()) {
        if (digitCount(landline.group()) == 11) {
          spans.add(new TextSpan(landline.start(), landline.end()));
        }
      }
      collect(LANDLINE_COMPACT.matcher(text), spans);
      return spans;
    }
  }

  private static void collect(Matcher matcher, List<TextSpan> spans) {
    while (matcher.find()) {
      spans.add(new TextSpan(matcher.start(), matcher.end()));
    }
  }

  private static void scanInternational(String text, List<TextSpan> spans) {
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) != '+') {
        continue;
      }
      if (i > 0 && Character.isDigit(text.charAt(i - 1))) {
        continue;
      }
      int start = i;
      int j = i + 1;
      if (j >= text.length() || text.charAt(j) < '1' || text.charAt(j) > '9') {
        continue;
      }
      j++;
      boolean broken = false;
      while (j < text.length()) {
        char c = text.charAt(j);
        if (Character.isDigit(c)) {
          j++;
          continue;
        }
        if (c == ' ' || c == '\t' || c == '.' || c == '-') {
          if (isSeparator(text.charAt(j - 1))) {
            broken = true;
            break;
          }
          j++;
          continue;
        }
        break;
      }
      if (broken) {
        continue;
      }
      int end = j;
      while (end > start + 1 && isSeparator(text.charAt(end - 1))) {
        end--;
      }
      if (digitCount(text.substring(start, end)) >= 8
          && digitCount(text.substring(start, end)) <= 15) {
        spans.add(new TextSpan(start, end));
        i = end - 1;
      }
    }
  }

  private static boolean isSeparator(char c) {
    return c == ' ' || c == '\t' || c == '.' || c == '-';
  }

  private static int digitCount(String value) {
    int count = 0;
    for (int i = 0; i < value.length(); i++) {
      if (Character.isDigit(value.charAt(i))) {
        count++;
      }
    }
    return count;
  }

  private static boolean singleSeparators(String raw) {
    boolean previous = false;
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      boolean separator = c == ' ' || c == '\t' || c == '-';
      if (separator && previous) {
        return false;
      }
      if (!separator && !Character.isDigit(c)) {
        return false;
      }
      previous = separator;
    }
    return true;
  }

  static String digits(String raw) {
    StringBuilder builder = new StringBuilder(raw.length());
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c >= '0' && c <= '9') {
        builder.append(c);
      }
    }
    return builder.toString();
  }

  private static String compact(String raw) {
    StringBuilder builder = new StringBuilder(raw.length());
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c == ' ' || c == '\t') {
        continue;
      }
      builder.append(c >= 'a' && c <= 'z' ? (char) (c - 32) : c);
    }
    return builder.toString();
  }

  private static boolean overlaps(List<TextSpan> spans, int start, int end) {
    for (TextSpan span : spans) {
      if (start < span.end() && span.start() < end) {
        return true;
      }
    }
    return false;
  }

  /** PEM block. Kept next to the other secret rules. */
  static final class Pem {
    static final Pattern BLOCK =
        Pattern.compile(
            "-----BEGIN (?:RSA |EC |OPENSSH |DSA |ENCRYPTED )?PRIVATE KEY-----[\\r\\n]+(?:[A-Za-z0-9+/=]+[\\r\\n]+)+-----END (?:RSA |EC |OPENSSH |DSA |ENCRYPTED )?PRIVATE KEY-----");

    private Pem() {}
  }

  /** IPv6 structural check: one compression at most, and a real hex digit. */
  static final class Ipv6 {
    private Ipv6() {}

    static boolean valid(String value) {
      if (value.indexOf(':') < 0) {
        return false;
      }
      boolean hex = false;
      int compressions = 0;
      for (int i = 0; i < value.length(); i++) {
        char c = value.charAt(i);
        if (c == ':') {
          if (i + 1 < value.length() && value.charAt(i + 1) == ':') {
            compressions++;
            i++;
          }
        } else if (isHex(c)) {
          hex = true;
        } else {
          return false;
        }
      }
      if (!hex || compressions > 1) {
        return false;
      }
      if (compressions == 1) {
        String[] sides = value.split("::", -1);
        int left = groups(sides[0]);
        int right = groups(sides[1]);
        return left >= 0 && right >= 0 && left + right < 8;
      }
      return groups(value) == 8;
    }

    private static int groups(String side) {
      if (side.isEmpty()) {
        return 0;
      }
      String[] parts = side.split(":", -1);
      for (String part : parts) {
        if (part.isEmpty() || part.length() > 4) {
          return -1;
        }
        for (int i = 0; i < part.length(); i++) {
          if (!isHex(part.charAt(i))) {
            return -1;
          }
        }
      }
      return parts.length;
    }

    private static boolean isHex(char c) {
      return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
  }
}
