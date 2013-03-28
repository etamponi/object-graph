/*
 * Copyright 2013 Emanuele Tamponi
 *
 * This file is part of object-graph.
 *
 * object-graph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * object-graph is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.objectgraph.core;

import java.util.*;

class ParentRegistry {

    private ParentRegistry() {
    }

    private final static Map<Node, Map<EventRecipient, Set<String>>> registry = new WeakHashMap<>();

    static void registerTree(Node root) {
        for (String property : root.getProperties()) {
            Object value = root.get(property);
            if (value instanceof Node && !registered(root, property, (Node) value)) {
                register(root, property, (Node) value);
                registerTree((Node) value);
            }
        }
    }

    static boolean registered(EventRecipient parent, String property, Node child) {
        if (!registry.containsKey(child))
            return false;
        if (!registry.get(child).containsKey(parent))
            return false;
        return registry.get(child).get(parent).contains(property);
    }

    static void register(EventRecipient parent, String property, Node child) {
        if (!registry.containsKey(child))
            registry.put(child, new WeakHashMap<EventRecipient, Set<String>>());
        if (!registry.get(child).containsKey(parent))
            registry.get(child).put(parent, new HashSet<String>());
        registry.get(child).get(parent).add(property);
    }

    static void unregister(EventRecipient parent, String property, Node child) {
        if (!registry.containsKey(child))
            return;
        if (!registry.get(child).containsKey(parent))
            return;
        registry.get(child).get(parent).remove(property);
        if (registry.get(child).get(parent).isEmpty()) // this child has no more that parent
            registry.get(child).remove(parent);
        if (registry.get(child).isEmpty()) // this child has no parents at all!
            registry.remove(child);
    }

    static Map<EventRecipient, Set<String>> getParentPaths(Node child) {
        if (!registry.containsKey(child))
            return Collections.emptyMap();
        return registry.get(child);
    }

}
