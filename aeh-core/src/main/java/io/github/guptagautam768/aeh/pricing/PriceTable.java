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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.github.guptagautam768.aeh.json.VersionedDocument;
import io.github.guptagautam768.aeh.schema.SchemaVersions;
import java.util.Map;

@JsonPropertyOrder({"schemaVersion", "version", "models"})
public record PriceTable(String schemaVersion, String version, Map<String, ModelPrice> models)
    implements VersionedDocument {

  public PriceTable {
    SchemaVersions.requireSupported(schemaVersion, "price table");
    if (version == null || version.isBlank()) {
      throw new IllegalArgumentException("price table version is required");
    }
    if (models == null) {
      models = Map.of();
    } else {
      models = Map.copyOf(models);
    }
  }

  public ModelPrice priceFor(String model) {
    return models.get(model);
  }
}
