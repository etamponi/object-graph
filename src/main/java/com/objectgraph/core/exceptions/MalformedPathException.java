package com.objectgraph.core.exceptions;

import com.objectgraph.core.RootedProperty;

public class MalformedPathException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final RootedProperty path;

    public MalformedPathException(RootedProperty path, String message) {
        super(message);
        this.path = path;
    }

    public RootedProperty getPath() {
        return path;
    }

}
