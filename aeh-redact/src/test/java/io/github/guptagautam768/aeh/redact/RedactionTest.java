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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.guptagautam768.aeh.HarnessVersion;
import io.github.guptagautam768.aeh.json.AehJson;
import io.github.guptagautam768.aeh.model.AgentRef;
import io.github.guptagautam768.aeh.model.Cassette;
import io.github.guptagautam768.aeh.model.ChatMessage;
import io.github.guptagautam768.aeh.model.Integration;
import io.github.guptagautam768.aeh.model.RedactionRecord;
import io.github.guptagautam768.aeh.model.Run;
import io.github.guptagautam768.aeh.model.RunConfig;
import io.github.guptagautam768.aeh.model.RunInput;
import io.github.guptagautam768.aeh.model.RunStatus;
import io.github.guptagautam768.aeh.model.ToolCall;
import io.github.guptagautam768.aeh.model.Usage;
import io.github.guptagautam768.aeh.redact.checksum.Checksums;
import io.github.guptagautam768.aeh.schema.SchemaVersions;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class RedactionTest {

  private static final JsonNodeFactory NODES = JsonNodeFactory.instance;
  private static final String EMAIL = "ada@example.com";
  private static final String OTHER_EMAIL = "grace@example.com";
  private static final String PHONE = "+1 202 555 0143";
  private static final String INDIAN_PHONE = "9876543210";
  private static final String CARD = "4111111111111111";
  private static final String IPV4 = "192.0.2.15";
  private static final String IPV6 = "2001:db8::1";
  private static final String IBAN = "DE89370400440532013000";
  private static final String PAN = "ABCPD1234F";
  private static final String OPENAI = "sk-testFAKEkeyNotReal0123456789abcdef";
  private static final String ANTHROPIC = "sk-ant-testFAKEkeyNotReal0123456789";
  private static final String AWS_KEY = "AKIAIOSFODNN7EXAMPLE";
  private static final String AWS_SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
  private static final String GITHUB = "ghp_" + "a".repeat(36);
  private static final String JWT = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.signaturevalue123456";
  private static final String HIGH_ENTROPY = "q7nP3mZ9xL2vR8tY5wQ1uI6oA4sD0fG7hJ2kLm";
  private static final String UUID = "550e8400-e29b-41d4-a716-446655440000";

  private final DefaultRedactor redactor = DefaultRedactor.create(RedactionPolicy.defaults());

  @Test
  void replacesEachPiiTypeAndReusesThePlaceholder() {
    String aadhaar = aadhaar("23456789012");
    String text =
        "Email "
            + EMAIL
            + " and "
            + EMAIL
            + ", call "
            + PHONE
            + " and "
            + INDIAN_PHONE
            + ", card "
            + CARD
            + ", host "
            + IPV4
            + ", v6 "
            + IPV6
            + ", iban "
            + IBAN
            + ", pan "
            + PAN
            + ", uid "
            + aadhaar;
    String redacted = redactText(text);

    assertThat(redacted)
        .contains("<EMAIL_1>")
        .contains("<PHONE_1>")
        .contains("<PHONE_2>")
        .contains("<CREDIT_CARD_1>")
        .contains("<IPV4_1>")
        .contains("<IPV6_1>")
        .contains("<IBAN_1>")
        .contains("<PAN_1>")
        .contains("<AADHAAR_1>")
        .doesNotContain(EMAIL)
        .doesNotContain("9876543210")
        .doesNotContain(CARD)
        .doesNotContain(aadhaar);
    assertThat(redacted).contains("<EMAIL_1> and <EMAIL_1>");
  }

  @Test
  void numbersPlaceholdersInSortedKeyOrder() {
    ObjectNode node = NODES.objectNode().put("b", OTHER_EMAIL).put("a", EMAIL);
    JsonNode redacted = redactor.redactTree(node).tree();
    assertThat(redacted.get("a").asText()).isEqualTo("<EMAIL_1>");
    assertThat(redacted.get("b").asText()).isEqualTo("<EMAIL_2>");
  }

  @Test
  void cardBeatsAShorterOverlappingPattern() {
    JsonNode redacted = redactor.redactTree(NODES.textNode("pay " + CARD + " now")).tree();
    assertThat(redacted.asText()).isEqualTo("pay <CREDIT_CARD_1> now");
    assertThat(redactor.redactTree(NODES.textNode("pay " + CARD + " now")).record().counts())
        .containsOnlyKeys("credit_card");
  }

  @Test
  void ignoresChecksumFailuresAndOrdinaryNumbers() {
    String aadhaar = aadhaar("23456789012");
    String badAadhaar = aadhaar.substring(0, 11) + (aadhaar.charAt(11) == '0' ? '1' : '0');
    String text = "card 4111111111111112 year 2024 order 12345 uid " + badAadhaar + " code AB12CD";
    assertThat(redactText(text)).isEqualTo(text);
  }

  @Test
  void redactsSyntheticSecrets() {
    String pem =
        """
                -----BEGIN PRIVATE KEY-----
                MIIEVQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAsynthetic
                -----END PRIVATE KEY-----
                """;
    String url = "https://user:s3cret@127.0.0.1/v1";
    String text =
        String.join(
            "\n",
            OPENAI,
            ANTHROPIC,
            AWS_KEY + " secret " + AWS_SECRET,
            GITHUB,
            JWT,
            pem.trim(),
            url);
    String redacted = redactText(text);

    assertThat(redacted)
        .doesNotContain(OPENAI)
        .doesNotContain(ANTHROPIC)
        .doesNotContain(AWS_KEY)
        .doesNotContain(AWS_SECRET)
        .doesNotContain(GITHUB)
        .doesNotContain(JWT)
        .doesNotContain("MIIEVQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAsynthetic")
        .doesNotContain("user:s3cret")
        .contains("<SECRET_")
        .contains("https://<SECRET_")
        .contains("@")
        .doesNotContain("user:s3cret");
  }

  @Test
  void entropyAppliesOnlyToKeyLikeFieldsAndHeaders() {
    ObjectNode node =
        NODES.objectNode().put("apiKey", HIGH_ENTROPY).put("note", HIGH_ENTROPY).put("token", UUID);
    JsonNode redacted = redactor.redactTree(node).tree();

    assertThat(redacted.get("apiKey").asText()).isEqualTo("<SECRET_1>");
    assertThat(redacted.get("note").asText()).isEqualTo(HIGH_ENTROPY);
    assertThat(redacted.get("token").asText()).isEqualTo(UUID);
    assertThat(Entropy.highEntropyToken(HIGH_ENTROPY)).isTrue();
    assertThat(Entropy.highEntropyToken(UUID)).isFalse();
    assertThat(Entropy.highEntropyToken("short-token")).isFalse();
  }

  @Test
  void dropsSecretHeadersAndKeepsTheRest() {
    ObjectNode headers =
        NODES.objectNode().put("Authorization", "Bearer " + OPENAI).put("X-Trace", "ok");
    ObjectNode node = NODES.objectNode().set("headers", headers);
    JsonNode redacted = redactor.redactTree(node).tree();

    assertThat(redacted.get("headers").has("Authorization")).isFalse();
    assertThat(redacted.get("headers").get("X-Trace").asText()).isEqualTo("ok");
    assertThat(redacted.toString()).doesNotContain(OPENAI);
  }

  @Test
  void allowlistDropsUnlistedFields() {
    RedactionPolicy policy =
        RedactionPolicy.defaults().withAllowlist(java.util.Set.of(), java.util.Set.of("note"));
    ObjectNode node = NODES.objectNode().put("note", "hello").put("body", EMAIL);
    JsonNode redacted = DefaultRedactor.create(policy).redactTree(node).tree();
    assertThat(redacted.has("body")).isFalse();
    assertThat(redacted.get("note").asText()).isEqualTo("hello");
  }

  @Test
  void customPatternAndServiceLoaderDetector() {
    RedactionPolicy policy =
        new RedactionPolicy(
            "default",
            DetectionPolicy.REDACT,
            false,
            java.util.Set.of(),
            java.util.Set.of(),
            java.util.Set.of(),
            List.of(new CustomPattern("ticket", Pattern.compile("SYNTH-\\d{4}"), "SECRET")),
            true,
            true,
            true);
    JsonNode redacted =
        DefaultRedactor.create(policy)
            .redactTree(NODES.textNode("ticket SYNTH-4242 marker SYNTHETIC_MARKER"))
            .tree();
    assertThat(redacted.asText()).doesNotContain("SYNTH-4242").doesNotContain("SYNTHETIC_MARKER");
    assertThat(redacted.asText()).contains("<SECRET_1>").contains("<SECRET_2>");
  }

  @Test
  void failModeAbortsWithoutEchoingTheValue() {
    DefaultRedactor failing =
        DefaultRedactor.create(RedactionPolicy.defaults().withOnDetect(DetectionPolicy.FAIL));
    Cassette cassette = cassetteWith(EMAIL);
    assertThatThrownBy(() -> failing.redact(cassette))
        .isInstanceOf(RedactionFailedException.class)
        .hasMessageNotContaining(EMAIL)
        .hasMessageContaining("email");
  }

  @Test
  void cassetteRedactionIsConsistentAcrossSteps() {
    Cassette redacted = redactor.redact(cassetteWith(EMAIL));
    String json =
        new String(AehJson.shared().write(redacted), java.nio.charset.StandardCharsets.UTF_8);

    assertThat(json).doesNotContain(EMAIL).contains("<EMAIL_1>");
    assertThat(redacted.redaction().policyId()).isEqualTo("default");
    assertThat(redacted.redaction().counts()).containsEntry("email", 2);
    assertThat(redacted.redaction().rulesApplied()).contains("email", "openai_key", "entropy");
    assertThat(redacted.runs().get(0).input().messages().get(0).content().asText())
        .contains("<EMAIL_1>");
    assertThat(redacted.runs().get(0).steps().get(0)).isInstanceOf(ToolCall.class);
  }

  @Test
  void verhoeffKnownVector() {
    assertThat(Checksums.verhoeff("2363")).isTrue();
    assertThat(Checksums.verhoeffCheckDigit("236")).isEqualTo('3');
    assertThat(Checksums.luhn(CARD)).isTrue();
    assertThat(Checksums.luhn("4111111111111112")).isFalse();
    assertThat(Checksums.iban(IBAN)).isTrue();
  }

  private String redactText(String text) {
    return redactor.redactTree(NODES.textNode(text)).tree().asText();
  }

  private static String aadhaar(String payload) {
    return payload + Checksums.verhoeffCheckDigit(payload);
  }

  private static Cassette cassetteWith(String email) {
    JsonNode arguments = NODES.objectNode().put("note", email);
    Run run =
        new Run(
            "run-1",
            new RunInput(
                List.of(
                    new ChatMessage(
                        "user", NODES.textNode("mail " + email), null, null, List.of())),
                Map.of()),
            new RunConfig("llama3.1:8b", "ollama", BigDecimal.ZERO, null, null, null),
            List.of(
                new ToolCall(
                    "tool", 0, 0, "lookupOrder", arguments, NODES.textNode("ok"), 1L, null)),
            "done",
            new Usage(1, 1, BigDecimal.ZERO, "builtin-2026.11"),
            1L,
            RunStatus.OK,
            null);
    return new Cassette(
        SchemaVersions.CURRENT,
        "redact.case",
        "support",
        Instant.parse("2026-11-12T02:15:00.000Z"),
        HarnessVersion.current(),
        new AgentRef("support", "1"),
        Integration.HTTP,
        RedactionRecord.none(),
        List.of(run));
  }
}
