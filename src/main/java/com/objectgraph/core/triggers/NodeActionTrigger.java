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

package com.objectgraph.core.triggers;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.objectgraph.core.Change;
import com.objectgraph.core.Event;
import com.objectgraph.core.Node;

public class NodeActionTrigger<N extends Node> extends StaticControlledPathTrigger<N> {

    private final String handlerMethod;

    public NodeActionTrigger(String handlerMethod, String... controlled) {
        super(controlled);
        this.handlerMethod = handlerMethod;
    }

    @Override
    protected boolean isTriggeredBy(Event event) {
        return (event.getType() instanceof Change);
    }

    @Override
    protected void action(Event event) {
        MethodAccess access = MethodAccess.get(getNode().getClass());
        access.invoke(getNode(), handlerMethod);
    }

}
