package com.opentable.components.filterorder;

import javax.servlet.Filter;

/**
 * Inject {@link OrderDeclaration} instances to assert the order in which filters should be run in the servlet
 * container.  Either specify that a filter comes last (there may be only one of these), or that a filter
 * <em>depends on</em> another (i.e., a filter must come after another).
 *
 * @see FilterOrderResolver
 */
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
