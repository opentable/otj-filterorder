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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * {@link FilterOrderResolver} resolves the order of servlet filters according to injected {@link OrderDeclaration}s.
 *
 * <p>
 * The total ordering is as follows.
 * If there is a last filter, it comes last.
 * Before that come filters for which an order was declared, in the order resolved by this class.
 * First come the filters for which no order was declared, in original injection order.
 *
 * <p>
 * If more than one last filter is declared, or if the order declarations create a cycle, {@link #resolve} throws
 * a runtime exception with helpful diagnostic information.
 *
 * <p>
 * Inject this just before you initialize the servlet container. It will call
 * {@link FilterRegistrationBean#setOrder}} on the beans to enforce their order.
 *
 * @see OrderDeclaration
 */
public class FilterOrderResolver {
    private static final Logger LOG = LoggerFactory.getLogger(FilterOrderResolver.class);

    private final List<FilterRegistrationBean<?>> filterRegistrationBeans;
    private final Collection<OrderDeclaration> orderDeclarations;

    @Inject
    public FilterOrderResolver(
            final List<FilterRegistrationBean<?>> filterRegistrationBeans,
            final Optional<Collection<OrderDeclaration>> orderDeclarations) {
        this.filterRegistrationBeans = filterRegistrationBeans;
        this.orderDeclarations = orderDeclarations.orElse(Collections.emptySet());
    }

    @PostConstruct
    void resolve() {
        if (orderDeclarations.isEmpty()) {
            LOG.debug("no order declarations; will not perform resolution");
            return;
        }

        final Map<FilterRegistrationBean<?>, Integer> originalIndexes = new HashMap<>();
        for (int i = 0; i < filterRegistrationBeans.size(); ++i) {
            final FilterRegistrationBean<?> frb = filterRegistrationBeans.get(i);
            originalIndexes.put(frb, i);
        }
        final Set<Class<? extends Filter>> last = new HashSet<>();
        final Map<Class<? extends Filter>, Collection<Class<? extends Filter>>> dependencies = new HashMap<>();
        orderDeclarations.forEach(od -> {
            if (od.last) {
                last.add(od.filter);
                return;
            }
            dependencies
                    .computeIfAbsent(od.filter, f -> new HashSet<>())
                    .add(od.dependsOn);
        });
        if (last.size() != 1) {
            throw new IllegalStateException(String.format("multiple last filters: %s", last));
        }
        final Optional<List<Class<? extends Filter>>> cycle = new CycleFinder<>(dependencies).run();
        if (cycle.isPresent()) {
            throw new IllegalStateException(String.format("filter cycle detected: %s", cycle.get()));
        }
        final List<FilterRegistrationBean<?>> sorted = new ArrayList<>(filterRegistrationBeans);
        sorted.sort((frb1, frb2) -> {
            final Class<? extends Filter> f1 = frb1.getFilter().getClass();
            final Class<? extends Filter> f2 = frb2.getFilter().getClass();
            // Last comes last.
            if (last.contains(f1)) {
                return 1;
            } else if (last.contains(f2)) {
                return -1;
            }
            // If neither is last, compare declared dependencies.
            final Collection<Class<? extends Filter>> deps1 = dependencies.get(f1);
            if (deps1 != null && deps1.contains(f2)) {
                return 1;
            }
            final Collection<Class<? extends Filter>> deps2 = dependencies.get(f2);
            if (deps2 != null && deps2.contains(f1)) {
                return -1;
            }
            // If there is a difference between the FRBs in whether they had any dependencies declared,
            // put ones that _did not_ declare dependencies first.
            if ((deps1 == null) != (deps2 == null)) {
                if (deps1 == null) {
                    return -1;
                }
                return 1;
            }
            // Otherwise, maintain original order.
            return Integer.compare(originalIndexes.get(frb1), originalIndexes.get(frb2));
        });
        for (int i = 0; i < sorted.size(); ++i) {
            sorted.get(i).setOrder(i);
        }
        LOG.info("resolved order of {} filter{}: {}", sorted.size(), sorted.size() != 1 ? "s" : "",
                sorted.stream().map(FilterRegistrationBean::getFilter).collect(Collectors.toList()));
    }
}
