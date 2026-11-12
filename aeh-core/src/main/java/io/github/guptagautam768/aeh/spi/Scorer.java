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
package io.github.guptagautam768.aeh.spi;

import io.github.guptagautam768.aeh.model.EvalCase;
import io.github.guptagautam768.aeh.model.Run;

/**
 * Scorer SPI (ADR-0006). Implementations arrive with the scoring pipeline. Scorers must not throw;
 * failures become {@code scorer_error} checks.
 */
public interface Scorer {

  String name();

  ScoreResult score(EvalCase evalCase, Run run, ScoringContext context);
}
