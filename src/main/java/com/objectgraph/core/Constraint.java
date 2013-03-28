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

import java.util.List;
import java.util.ListIterator;

public abstract class Constraint<N extends Node, T> extends ErrorCheck<N> {

    private final String path;

    public Constraint(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    protected abstract String check(T element);

    @Override
    public Error getError() {
        T value = getNode().get(path);
        if (value == null)
            return null;
        String check = check((T) value);
        if (check != null)
            return new Error(Error.ErrorLevel.SEVERE, path + ": " + check);
        else
            return null;
    }

    public final void filter(List<T> list) {
        ListIterator<T> it = list.listIterator();
        while (it.hasNext()) {
            T t = it.next();
            if (check(t) != null)
                it.remove();
        }
    }

}
