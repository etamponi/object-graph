package com.objectgraph.core;

import java.util.*;

class ParentRegistry {

    private ParentRegistry() {
    }

    private final static Map<Node, Map<Node, Set<String>>> registry = new WeakHashMap<>();

    static void registerTree(Node root) {
        for (String property : root.getProperties()) {
            Object value = root.get(property);
            if (value instanceof Node && !registered(root, property, (Node) value)) {
                register(root, property, (Node) value);
                registerTree((Node) value);
            }
        }
    }

    static boolean registered(Node parent, String property, Node child) {
        if (!registry.containsKey(child))
            return false;
        if (!registry.get(child).containsKey(parent))
            return false;
        return registry.get(child).get(parent).contains(property);
    }

    static void register(Node parent, String property, Node child) {
        if (!registry.containsKey(child))
            registry.put(child, new WeakHashMap<Node, Set<String>>());
        if (!registry.get(child).containsKey(parent))
            registry.get(child).put(parent, new HashSet<String>());
        registry.get(child).get(parent).add(property);
    }

    static void unregister(Node parent, String property, Node child) {
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

    static Map<Node, Set<String>> getParentPaths(Node child) {
        if (!registry.containsKey(child))
            return Collections.emptyMap();
        return registry.get(child);
    }

}
