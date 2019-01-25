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

import javax.servlet.Filter;

/**
 * Inject {@link OrderDeclaration} instances to assert the order in which filters should be run in the servlet
 * container.  Either specify that a filter comes last (there may be only one of these), or that a filter
 * <em>depends on</em> another (i.e., a filter must come after another).
 *
 * <p>
 * Note that this implementation uses the {@link Filter} classes themselves to declare dependencies. So if there are
 * multiple filters in your chain of the same class (e.g., with different configurations), this limits the
 * expressiveness of this library.
 *
 * @see FilterOrderResolver
 */
public final class OrderDeclaration {
    final boolean isLast;
    final Class<? extends Filter> filter;
    final Class<? extends Filter> dependsOn;

    OrderDeclaration(
            final boolean isLast,
            final Class<? extends Filter> filter,
            final Class<? extends Filter> dependsOn) {
        this.isLast = isLast;
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

        /** In other words, {@link #filter} will &ldquo;come after&rdquo; the param {@code filter} here. */
        public OrderDeclaration dependsOn(final Class<? extends Filter> filter) {
            return new OrderDeclaration(false, this.filter, filter);
        }
    }
}
