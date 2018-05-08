package com.opentable.components.filterorder;

import javax.servlet.Filter;

public final class OrderDeclaration {
    final boolean last;
    final Class<? extends Filter> filter, dependsOn;

    OrderDeclaration(
            final boolean last,
            final Class<? extends Filter> filter,
            final Class<? extends Filter> dependsOn) {
        this.last = last;
        this.filter = filter;
        this.dependsOn = dependsOn;
    }

    public static Builder of(final Class<? extends Filter> filter) {
        return new Builder(filter);
    }

    public static OrderDeclaration last(final Class<? extends Filter> filter) {
        return new OrderDeclaration(true, filter, null);
    }

    public static class Builder {
        private final Class<? extends Filter> filter;

        Builder(final Class<? extends Filter> filter) {
            this.filter = filter;
        }

        public OrderDeclaration dependsOn(final Class<? extends Filter> filter) {
            return new OrderDeclaration(false, this.filter, filter);
        }

        public OrderDeclaration comesAfter(final Class<? extends Filter> filter) {
            return dependsOn(filter);
        }
    }
}
