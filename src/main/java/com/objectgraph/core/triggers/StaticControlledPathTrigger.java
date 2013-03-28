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

import com.objectgraph.core.Node;
import com.objectgraph.core.Trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class StaticControlledPathTrigger<N extends Node> extends Trigger<N> {

    private final List<String> controlledPaths;

    public StaticControlledPathTrigger(String... controlled) {
        controlledPaths = new ArrayList<>(Arrays.asList(controlled));
    }

    @Override
    public List<String> getControlledPaths() {
        return Collections.unmodifiableList(controlledPaths);
    }

}
