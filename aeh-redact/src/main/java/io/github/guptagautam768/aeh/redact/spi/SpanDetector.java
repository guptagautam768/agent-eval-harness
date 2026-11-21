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
package io.github.guptagautam768.aeh.redact.spi;

import java.util.List;

/**
 * Code-based detector (SEC-009). Built-ins are registered explicitly so their order is stable.
 * Extra detectors are loaded with {@link java.util.ServiceLoader}.
 */
public interface SpanDetector {

  String ruleId();

  /** Placeholder type, for example {@code EMAIL} or {@code SECRET}. */
  String placeholderType();

  /** Lower wins when two spans have the same length (ADR-0014, proposed). */
  int priority();

  List<TextSpan> detect(String text);
}
