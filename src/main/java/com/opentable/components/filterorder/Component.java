package com.opentable.components.filterorder;

/**
 * A simple Spring managed bean.
 * You'll probably want to do something more than
 * store a String.
 */
public class Component {
    private final String configurable;

    public Component(String configurable) {
        this.configurable = configurable;
    }

    public String getConfigurable() {
        return configurable;
    }
}
