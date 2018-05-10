/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opentable.components.filterorder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CycleFinderTest {
    @Test
    public void testTrivial() {
        final Optional<List<Object>> cycle = new CycleFinder<>(Collections.emptyMap()).run();
        Assertions.assertThat(cycle).isNotPresent();
    }

    @Test
    public void testSingle() {
        final Optional<List<String>> cycle = new CycleFinder<>(
                Collections.singletonMap("a", Collections.singletonList("b"))
        ).run();
        Assertions.assertThat(cycle).isNotPresent();
    }

    @Test
    public void testSelfLoop() {
        final Optional<List<String>> cycle = new CycleFinder<>(
                Collections.singletonMap("a", Collections.singletonList("a"))
        ).run();
        Assertions.assertThat(cycle).isPresent();
        Assertions.assertThat(cycle.get()).isNotNull();
        Assertions.assertThat(cycle.get()).isNotEmpty();
        Assertions.assertThat(cycle.get()).containsExactlyInAnyOrder("a", "a");
    }

    @Test
    public void testSimple() {
        final Optional<List<String>> cycle = new CycleFinder<>(
                ImmutableMap.of(
                        "a", Collections.singletonList("b"),
                        "b", Collections.singletonList("a")

                )
        ).run();
        Assertions.assertThat(cycle).isPresent();
        Assertions.assertThat(cycle.get()).isNotNull();
        Assertions.assertThat(cycle.get()).isNotEmpty();
        Assertions.assertThat(cycle.get()).containsExactlyInAnyOrder("a", "b", "a");
    }

    @Test
    public void testTransitive() {
        final Optional<List<String>> cycle = new CycleFinder<>(
                ImmutableMap.of(
                        "a", Collections.singletonList("b"),
                        "b", Collections.singletonList("c"),
                        "c", Collections.singletonList("a")

                )
        ).run();
        Assertions.assertThat(cycle).isPresent();
        Assertions.assertThat(cycle.get()).isNotNull();
        Assertions.assertThat(cycle.get()).isNotEmpty();
        Assertions.assertThat(cycle.get()).containsExactlyInAnyOrder("a", "b", "c", "a");
    }

    @Test
    public void testY() {
        final Optional<List<String>> cycle = new CycleFinder<>(
                ImmutableMap.of(
                        "a", Collections.singletonList("b"),
                        "c", Collections.singletonList("b")
                )
        ).run();
        Assertions.assertThat(cycle).isNotPresent();
    }

    @Test
    public void testNonTrivial() {
        final Optional<List<String>> cycle = new CycleFinder<>(
                ImmutableMap.of(
                        "d", Collections.singletonList("a"),
                        "a", ImmutableList.of("b", "c"),
                        "c", Collections.singletonList("b")
                )
        ).run();
        Assertions.assertThat(cycle).isNotPresent();
    }
}
