The filter order dependency resolver accepts dependency declarations in
the form of injected beans and reorders servlet filters in order to
accord to the desired dependencies.

Here is an sample use.  In it, B is said to depend on A, so in the final
ordering, A will come before B.

    @Bean
    public OrderDeclaration testOrderDeclaration() {
        return OrderDeclaration
                .of(FilterB.class)
                .dependsOn(FilterA.class);
    }


See the classes' Javadocs for more detail.
