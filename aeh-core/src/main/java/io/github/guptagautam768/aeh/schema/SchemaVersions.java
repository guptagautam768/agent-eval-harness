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
package io.github.guptagautam768.aeh.schema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Readers accept the current minor and the previous minor of the same major (NFR-007). A major bump
 * will ship {@code aeh migrate} (ADR-0003); that command is not part of week 1.
 */
public final class SchemaVersions {

  public static final String CURRENT = "1.0";

  private static final int CURRENT_MAJOR = 1;
  private static final int CURRENT_MINOR = 0;
  private static final Pattern WIRE = Pattern.compile("([0-9]+)\\.([0-9]+)");

  private SchemaVersions() {}

  public static SchemaVersion parse(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("schemaVersion is required");
    }
    Matcher matcher = WIRE.matcher(raw);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("schemaVersion must be major.minor, got: " + raw);
    }
    return new SchemaVersion(
        Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
  }

  public static boolean isSupported(String raw) {
    return isSupported(parse(raw), CURRENT_MAJOR, CURRENT_MINOR);
  }

  /** Visible for tests that simulate a later minor without changing {@link #CURRENT}. */
  public static boolean isSupported(String raw, int currentMajor, int currentMinor) {
    return isSupported(parse(raw), currentMajor, currentMinor);
  }

  public static boolean isSupported(SchemaVersion version, int currentMajor, int currentMinor) {
    if (version.major() != currentMajor || currentMinor < 0) {
      return false;
    }
    return version.minor() == currentMinor || version.minor() == currentMinor - 1;
  }

  public static void requireSupported(String raw, String document) {
    if (!isSupported(raw)) {
      throw new UnsupportedSchemaException(
          document, String.valueOf(raw), "current " + CURRENT + " and previous minor");
    }
  }
}
