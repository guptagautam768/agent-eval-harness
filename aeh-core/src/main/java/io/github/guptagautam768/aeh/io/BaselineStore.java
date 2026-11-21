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

import io.github.guptagautam768.aeh.model.Baseline;
import java.nio.file.Path;

/** Baseline JSON I/O (ADR-0008). Promotion policy is enforced by the CLI in a later week. */
public final class BaselineStore {

  private final JsonDocuments documents;

  public BaselineStore() {
    this(new JsonDocuments());
  }

  public BaselineStore(JsonDocuments documents) {
    this.documents = documents;
  }

  public byte[] toBytes(Baseline baseline) {
    return documents.write(baseline);
  }

  public Baseline read(byte[] bytes) {
    return documents.read(bytes, Baseline.class);
  }

  public Baseline read(Path path) {
    return documents.read(path, Baseline.class);
  }

  public void write(Path path, Baseline baseline) {
    documents.write(path, baseline);
  }
}
