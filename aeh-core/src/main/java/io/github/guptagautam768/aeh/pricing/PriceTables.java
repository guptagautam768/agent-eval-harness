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
package io.github.guptagautam768.aeh.pricing;

import io.github.guptagautam768.aeh.json.AehJson;
import io.github.guptagautam768.aeh.json.JsonDocumentException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PriceTables {

  private static final String RESOURCE = "default-price-table.json";

  private PriceTables() {}

  public static PriceTable builtin() {
    InputStream stream = PriceTables.class.getResourceAsStream(RESOURCE);
    if (stream == null) {
      throw new IllegalStateException("Missing built-in price table resource " + RESOURCE);
    }
    try (InputStream in = stream) {
      return read(in.readAllBytes());
    } catch (IOException exception) {
      throw new JsonDocumentException("Could not read built-in price table", exception);
    }
  }

  public static PriceTable load(Path path) {
    try {
      return read(Files.readAllBytes(path));
    } catch (IOException exception) {
      throw new JsonDocumentException("Could not read price table " + path, exception);
    }
  }

  public static PriceTable read(byte[] bytes) {
    return AehJson.shared().read(bytes, PriceTable.class);
  }

  public static byte[] write(PriceTable table) {
    return AehJson.shared().write(table);
  }
}
