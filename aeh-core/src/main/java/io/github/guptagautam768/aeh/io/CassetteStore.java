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

import io.github.guptagautam768.aeh.json.JsonDocumentException;
import io.github.guptagautam768.aeh.model.Cassette;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pretty-printed cassette I/O. The default size warning is 1 MiB (ADR-0003). Bytes must already be
 * redacted; this store does not call the redactor.
 */
public final class CassetteStore {

  public static final long DEFAULT_WARN_BYTES = 1024L * 1024L;

  private static final Logger log = LoggerFactory.getLogger(CassetteStore.class);

  private final JsonDocuments documents;
  private final long warnBytes;

  public CassetteStore() {
    this(new JsonDocuments(), DEFAULT_WARN_BYTES);
  }

  public CassetteStore(JsonDocuments documents, long warnBytes) {
    this.documents = documents;
    this.warnBytes = warnBytes;
  }

  public byte[] toBytes(Cassette cassette) {
    return documents.write(cassette);
  }

  public Cassette read(byte[] bytes) {
    return documents.read(bytes, Cassette.class);
  }

  public Cassette read(Path path) {
    return documents.read(path, Cassette.class);
  }

  public WriteReceipt write(Path path, Cassette cassette) {
    byte[] bytes = toBytes(cassette);
    List<String> warnings = new ArrayList<>();
    if (bytes.length > warnBytes) {
      String warning =
          "Cassette "
              + cassette.caseId()
              + " is "
              + bytes.length
              + " bytes, above the "
              + warnBytes
              + " byte warning threshold";
      warnings.add(warning);
      log.warn(warning);
    }
    try {
      AtomicFiles.write(path, bytes);
    } catch (IOException exception) {
      throw new JsonDocumentException("Could not write cassette " + path, exception);
    }
    return new WriteReceipt(path, bytes.length, warnings);
  }
}
