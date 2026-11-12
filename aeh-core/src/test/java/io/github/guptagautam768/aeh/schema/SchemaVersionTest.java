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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SchemaVersionTest {

  @Test
  void currentMinorAndPreviousMinorAreAccepted() {
    assertThat(SchemaVersions.isSupported("1.0")).isTrue();
    assertThat(SchemaVersions.isSupported("1.1")).isFalse();
    assertThat(SchemaVersions.isSupported("0.9")).isFalse();
    assertThat(SchemaVersions.isSupported("2.0")).isFalse();

    assertThat(SchemaVersions.isSupported("1.0", 1, 1)).isTrue();
    assertThat(SchemaVersions.isSupported("1.1", 1, 1)).isTrue();
    assertThat(SchemaVersions.isSupported("1.2", 1, 1)).isFalse();
    assertThat(SchemaVersions.isSupported("2.1", 1, 1)).isFalse();
  }

  @Test
  void rejectsMalformedVersions() {
    assertThatThrownBy(() -> SchemaVersions.parse("1"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SchemaVersions.parse("1.0.0"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SchemaVersions.parse(null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
