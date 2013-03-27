package com.objectgraph.core;

import java.lang.ref.WeakReference;
import java.util.List;

public class RootedProperty {

    private final WeakReference<Node> root;
    private final String path;

    RootedProperty(Node root, String path) {
        this.root = new WeakReference<>(root);
        this.path = path;
    }

    public Node getRoot() {
        return root.get();
    }

    public String getPath() {
        return path;
    }

    public <T> T getValue() {
        return root.get().get(path);
    }

    public <T> T getValue(Class<T> type) {
        return root.get().get(path, type);
    }

    public void setValue(Object content) {
        root.get().set(path, content);
    }

    public Class<?> getValueType(boolean runtime) {
        return root.get().getPropertyType(path, runtime);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((root == null) ? 0 : root.get().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RootedProperty other = (RootedProperty) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (root == null) {
            if (other.root != null)
                return false;
        } else if (root.get() != other.root.get())
            return false;
        return true;
    }

    public List<?> getPossibleValues() {
        return root.get().getPossiblePropertyValues(path);
    }

}
