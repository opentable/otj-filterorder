package com.opentable.components.filterorder;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Handy Spring configuration class to use the {@link FilterOrderResolver}.
 *
 * @see FilterOrderResolver
 * @see OrderDeclaration
 */
@Configuration
@Import(FilterOrderResolver.class)
public class FilterOrderResolverConfiguration {
}
