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
