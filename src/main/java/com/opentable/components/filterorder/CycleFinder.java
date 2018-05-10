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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Given a mapping of graph nodes to other nodes (&ldquo;edges&rdquo;), call {@link #run} to find a cycle, if there
 * is one.
 * @param <T> node type
 */
class CycleFinder<T> {
    private final Set<T> seen = new HashSet<>();
    private final Map<T, Collection<T>> edges;

    CycleFinder(final Map<T, Collection<T>> edges) {
        this.edges = edges;
    }

    /** Return a list of cyclic nodes, if there is one, otherwise empty. */
    Optional<List<T>> run() {
        for (final T node : edges.keySet()) {
            final Optional<List<T>> cycle = dfs(node);
            if (cycle.isPresent()) {
                return cycle;
            }
        }
        return Optional.empty();
    }

    /** Kick off the DFS and massage the return type. */
    private Optional<List<T>> dfs(final T start) {
        seen.clear();
        final List<T> cycle = findCycle(start);
        if (cycle.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cycle);
    }

    /** Recursive DFS. */
    private List<T> findCycle(final T node) {
        boolean newlySeen = seen.add(node);
        if (!newlySeen) {
            // Cycle!
            final List<T> cycle = new ArrayList<>();
            cycle.add(node);
            return cycle;
        }
        // We need to go deeper.
        for (final T next : edges.getOrDefault(node, Collections.emptySet())) {
            final List<T> cycle = findCycle(next);
            if (!cycle.isEmpty()) {
                cycle.add(node);
                return cycle;
            }
            // If no cycle found in the just-finished recursion, remove from seen to avoid mis-identifying cycle across
            // separate DFS traversals that happen to share a node.
            seen.remove(next);
        }
        // No cycle found at this point.
        return Collections.emptyList();
    }
}
