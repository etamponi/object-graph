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
import com.objectgraph.core.Event;
import com.objectgraph.core.Node;
import com.objectgraph.core.Change;
import com.objectgraph.utils.PathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dependency extends StaticControlledPathTrigger<Node> {

    private final String handlerMethod;
    private final String controlledPath;
    private final List<String> parameterPaths;
    private final Object handlerObject;

    public Dependency(String controlled, String handlerMethod, String... parameters) {
        super(controlled);
        this.controlledPath = controlled;
        this.handlerMethod = handlerMethod;
        this.handlerObject = null;
        this.parameterPaths = new ArrayList<>(Arrays.asList(parameters));
    }

    public Dependency(String controlled, Object handlerObject, String handlerMethod, String... parameters) {
        super(controlled);
        this.controlledPath = controlled;
        this.handlerMethod = handlerMethod;
        this.handlerObject = handlerObject;
        this.parameterPaths = new ArrayList<>(Arrays.asList(parameters));
        if (handlerObject != null) {
            // TODO Add checks if possible
        }
    }

    @Override
    protected boolean isTriggeredBy(Event event) {
        if (event.getType() instanceof Change) {
            if (PathUtils.isPrefix(event.getPath(), controlledPath))
                return true;

            for (String path : parameterPaths)
                if (PathUtils.samePrefix(event.getPath(), path))
                    return true;

            return false;
        } else {
            return false;
        }
    }

    @Override
    protected void action(Event event) {
        Object handler = handlerObject == null ? getNode() : handlerObject;
        MethodAccess access = MethodAccess.get(handler.getClass());
        Object[] params = new Object[parameterPaths.size()];
        int i = 0;
        for (String path : parameterPaths)
            params[i++] = getNode().get(path);
        getNode().set(controlledPath, access.invoke(handler, handlerMethod, params));
    }

}
