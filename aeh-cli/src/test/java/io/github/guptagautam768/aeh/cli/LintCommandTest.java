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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guptagautam768.aeh.ExitCodes;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class LintCommandTest {

  @TempDir Path temp;

  @Test
  void lintExitCodes() throws Exception {
    Path planted = temp.resolve("secret.txt");
    Files.writeString(planted, "token sk-testFAKEkeyNotReal0123456789abcdef\n");
    Path clean = temp.resolve("clean.txt");
    Files.writeString(clean, "hello <EMAIL_1>\n");

    assertThat(new CommandLine(new AehMain()).execute()).isEqualTo(ExitCodes.USAGE);
    assertThat(new CommandLine(new AehMain()).execute("lint")).isEqualTo(ExitCodes.USAGE);
    assertThat(new CommandLine(new AehMain()).execute("lint", planted.toString()))
        .isEqualTo(ExitCodes.INTEGRITY);
    assertThat(new CommandLine(new AehMain()).execute("lint", clean.toString()))
        .isEqualTo(ExitCodes.PASS);
    assertThat(new CommandLine(new AehMain()).execute("lint", temp.resolve("nope").toString()))
        .isEqualTo(ExitCodes.USAGE);
  }
}
