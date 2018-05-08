package com.opentable.components;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ComponentTest {
    @Test
    public void testComponent() {
        final String foo = "foo";
        Component c = new Component(foo);
        assertThat(c.getConfigurable()).isEqualTo(foo);
    }
}
