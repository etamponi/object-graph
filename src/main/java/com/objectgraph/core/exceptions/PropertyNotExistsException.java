package com.objectgraph.core.exceptions;

import com.objectgraph.core.RootedProperty;

public class PropertyNotExistsException extends MalformedPathException {

    private static final long serialVersionUID = 1L;

    public PropertyNotExistsException(RootedProperty path) {
        super(path, "No such property");
    }

}
