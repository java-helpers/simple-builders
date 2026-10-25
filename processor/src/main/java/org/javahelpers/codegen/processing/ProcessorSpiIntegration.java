/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.javahelpers.codegen.processing;

import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.util.Elements;

/**
 * The lifecycle an SPI integration shares between a processor and the SPI adapters of other
 * frameworks on the same annotation-processor path (and therefore the same classloader).
 *
 * <p>One instance is owned by the processor's domain holder (usually a static field). The processor
 * transitions it alone — SPI adapters only ever read it, they never initialize or mutate it. All
 * state is compilation-scoped: javac initializes each processor lazily when its turn in a round
 * comes, so before the processor has run its {@code init()} nothing can be trusted — values may be
 * leftovers of a previous compilation in a long-lived JVM (Gradle daemon, incremental builds).
 * Adapters therefore pass their own {@link Elements} instance to every read: javac hands every
 * processor of one compilation the same {@code Elements} instance, so a mismatch means this holder
 * still describes an older run.
 */
public final class ProcessorSpiIntegration {

  /**
   * Where the owning processor's generation stands in the current compilation, as SPI adapters
   * observe it.
   */
  public enum State {
    /**
     * No processor init happened in this compilation yet — published content may be a previous
     * run's, and the registry must be treated as possibly still filling.
     */
    INIT,
    /**
     * The processor initialized: its generating round may not have run yet — the registry must be
     * treated as possibly still filling.
     */
    PROCESSING,
    /** The generating round ran: the registry is final for this compilation. */
    FINISHED
  }

  private volatile State state = State.INIT;

  /** The {@link Elements} of the compilation this holder's content describes. */
  private final AtomicReference<Elements> compilationElements = new AtomicReference<>();

  /** Starts a new compilation: marks generation as running for the given compilation. */
  public void initCompilation(Elements elements) {
    compilationElements.set(elements);
    state = State.PROCESSING;
  }

  /** Marks generation final: the generating round ran (or the compilation ended without one). */
  public void finishCompilation() {
    state = State.FINISHED;
  }

  /**
   * Whether {@code observed} belongs to the compilation this holder currently describes — {@code
   * false} while it still carries a previous run's leftovers. javac creates one {@link Elements}
   * per compilation and shares it between all processors, so reference identity is a reliable
   * staleness check that never requires the SPI adapters to write anything.
   */
  public boolean isCurrentCompilation(Elements observed) {
    return observed != null && observed == compilationElements.get();
  }

  /** The lifecycle state of the current compilation. */
  public State state() {
    return state;
  }

  /**
   * Whether the owning processor finished generating for the compilation {@code observed} belongs
   * to — {@code false} while this holder still describes another run.
   *
   * <p>The guard is not optional: an SPI holder's statics live in the processor-path classloader,
   * which build tools reuse across compilations (Gradle daemon, in-process javac, IDE builds,
   * repeated compiles in one JVM). In a new compilation's first round — before the owning
   * processor's {@code init()} had its turn — {@code state} can still be the previous run's {@link
   * State#FINISHED} and the registry still holds its entries. Trusting them would claim outputs by
   * name they were never planned with here and answer misses as final instead of deferring. javac
   * hands every processor of one compilation the same {@link Elements} instance and a different one
   * per compilation, so identity is the read-only, order-independent boundary marker — an SPI-side
   * reset cannot work, because SPI init order against the owning processor's init is path-order
   * dependent.
   */
  public boolean isFinishedForIntegration(Elements observed) {
    return isCurrentCompilation(observed) && state == State.FINISHED;
  }
}
