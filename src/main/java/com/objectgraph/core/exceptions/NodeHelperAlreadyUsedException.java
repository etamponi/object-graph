package com.objectgraph.core.exceptions;/*
 * Copyright 2013 emanuele
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

 * You should have received a copy of the GNU General Public License
 * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.objectgraph.core.Node;
import com.objectgraph.core.NodeHelper;

public class NodeHelperAlreadyUsedException extends RuntimeException {
    public NodeHelperAlreadyUsedException(NodeHelper helper, Node node) {
        super("NodeHelper " + helper + " is used by " + node);
    }
}
