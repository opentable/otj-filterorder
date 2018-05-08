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
            throw new RuntimeException(String.format("multiple last filters: %s", last));
        }
        final Optional<List<Class<? extends Filter>>> cycle = new CycleFinder<>(dependencies).run();
        if (cycle.isPresent()) {
            throw new RuntimeException(String.format("filter cycle detected: %s", cycle.get()));
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
