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
