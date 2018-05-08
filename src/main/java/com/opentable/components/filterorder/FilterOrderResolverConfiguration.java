package com.opentable.components.filterorder;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FilterOrderResolver.class)
public class FilterOrderResolverConfiguration {
}
