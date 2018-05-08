package com.opentable.components.filterorder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
}
