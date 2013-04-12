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

import com.objectgraph.pluginsystem.PluginManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RootedProperty {

    private final WeakReference<Node> root;
    private final String property;
    private final List<ErrorCheck<?,?>> errorChecks;

    RootedProperty(Node root, String property) {
        if (!root.hasProperty(property)) {
            throw new PropertyNotExistsException(root, property);
        }

        this.root = new WeakReference<>(root);
        this.property = property;
        errorChecks = root.getErrorChecks(property);
    }

    public Node getRoot() {
        return root.get();
    }

    public String getProperty() {
        return property;
    }

    public <T> T getValue() {
        return root.get().get(property);
    }

    public <T> T getValue(Class<T> type) {
        return root.get().get(property, type);
    }

    public void setValue(Object content) {
        root.get().set(property, content);
    }

    public Class<?> getValueType(boolean runtime) {
        return root.get().getPropertyType(property, runtime);
    }

    public List<ErrorCheck<?,?>> getErrorChecks(Error.Level minLevel) {
        List<ErrorCheck<?,?>> constraints = new ArrayList<>(errorChecks);

        Iterator<ErrorCheck<?,?>> it = constraints.iterator();
        while(it.hasNext()) {
            if (it.next().getLevel().ordinal() < minLevel.ordinal())
                it.remove();
        }

        return constraints;
    }

    public List<Error> getErrors() {
        return getErrors(getValue());
    }

    public List<Error> getErrors(Object value) {
        List<Error> ret = new ArrayList<>();
        for (ErrorCheck check: errorChecks) {
            Error error = check.getError(value);
            if (error != null) {
                ret.add(error);
            }
        }
        return ret;
    }

    public void updateErrorChecks() {
        errorChecks.clear();
        errorChecks.addAll(root.get().getErrorChecks(property));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((property == null) ? 0 : property.hashCode());
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
        if (property == null) {
            if (other.property != null)
                return false;
        } else if (!property.equals(other.property))
            return false;
        if (root == null) {
            if (other.root != null)
                return false;
        } else if (root.get() != other.root.get())
            return false;
        return true;
    }

}
