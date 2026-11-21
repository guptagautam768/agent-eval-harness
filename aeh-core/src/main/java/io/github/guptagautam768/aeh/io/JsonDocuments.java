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

import io.github.guptagautam768.aeh.json.AehJson;
import io.github.guptagautam768.aeh.json.JsonDocumentException;
import io.github.guptagautam768.aeh.json.VersionedDocument;
import io.github.guptagautam768.aeh.schema.SchemaVersions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Shared read/write path for versioned harness documents. */
public final class JsonDocuments {

  private final AehJson json;

  public JsonDocuments() {
    this(AehJson.shared());
  }

  public JsonDocuments(AehJson json) {
    this.json = json;
  }

  public byte[] write(VersionedDocument document) {
    SchemaVersions.requireSupported(document.schemaVersion(), document.getClass().getSimpleName());
    return json.write(document);
  }

  public <T extends VersionedDocument> T read(byte[] bytes, Class<T> type) {
    T value = json.read(bytes, type);
    SchemaVersions.requireSupported(value.schemaVersion(), type.getSimpleName());
    return value;
  }

  public void write(Path path, VersionedDocument document) {
    try {
      AtomicFiles.write(path, write(document));
    } catch (IOException exception) {
      throw new JsonDocumentException("Could not write " + path, exception);
    }
  }

  public <T extends VersionedDocument> T read(Path path, Class<T> type) {
    try {
      return read(Files.readAllBytes(path), type);
    } catch (IOException exception) {
      throw new JsonDocumentException("Could not read " + path, exception);
    }
  }
}
