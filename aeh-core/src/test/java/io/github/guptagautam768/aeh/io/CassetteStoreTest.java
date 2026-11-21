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
package io.github.guptagautam768.aeh.io;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guptagautam768.aeh.model.Cassette;
import io.github.guptagautam768.aeh.model.ModelFixtures;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CassetteStoreTest {

  @TempDir Path temp;

  @Test
  void writesAtomicallyAndWarnsAboveTheThreshold() throws IOException {
    Cassette cassette = ModelFixtures.cassette();
    CassetteStore store = new CassetteStore(new JsonDocuments(), 1);
    Path target = temp.resolve("cassettes").resolve("refund.lookup.json");

    WriteReceipt receipt = store.write(target, cassette);

    assertThat(Files.readAllBytes(target)).isEqualTo(store.toBytes(cassette));
    assertThat(store.read(target)).isEqualTo(store.read(store.toBytes(cassette)));
    assertThat(receipt.warnings()).hasSize(1);
    assertThat(receipt.warnings().get(0))
        .contains("refund.lookup")
        .doesNotContain("where is order");
    try (var leftovers = Files.list(target.getParent())) {
      assertThat(leftovers.filter(path -> path.getFileName().toString().endsWith(".tmp")))
          .isEmpty();
    }
  }
}
