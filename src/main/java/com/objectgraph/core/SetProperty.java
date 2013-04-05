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

public class SetProperty extends Change {

    private final RootedProperty property;
    private final Object oldContent;
    private final Object newContent;

    public SetProperty(RootedProperty property, Object oldContent, Object newContent) {
        this.property = property;
        this.oldContent = oldContent;
        this.newContent = newContent;
    }

    public RootedProperty getProperty() {
        return property;
    }

    public Object getOldValue() {
        return oldContent;
    }

    public Object getNewValue() {
        return newContent;
    }

}
