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

import com.objectgraph.utils.PathUtils;

/**
 *
 */
public class Event {

    private final String path;

    private final EventType type;

    /**
     *
     * @param path
     * @param type
     */
    public Event(String path, EventType type) {
        this.path = path;
        this.type = type;
    }

    /**
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     *
     * @return
     */
    public EventType getType() {
        return type;
    }

    /**
     *
     * @param cls
     * @param <ET>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <ET extends EventType> ET getType(Class<ET> cls) {
        return (ET) type;
    }

    /**
     *
     * @param parent
     * @return
     */
    public Event backPropagate(String parent) {
        return new Event(PathUtils.appendPath(parent, path), type);
    }

}
