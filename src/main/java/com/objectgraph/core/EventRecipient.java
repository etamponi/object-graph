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

import org.pcollections.PSet;

/**
 * An interface that defines objects that can be reached by {@link Event}s and that can handle them.
 * <p/>
 * The EventRecipient interface is needed to define various types of object in an event dispatch graph. The main type of
 * object in such a graph is a {@link Node}, but you can also define a {@link com.objectgraph.gui.PropertyEditor}, that
 * sits at the "root" of the graph and that does not dispatch events any further.
 * <p/>
 * This interface is for internal use and should be reimplemented with great care.
 */
public interface EventRecipient {

    /**
     * Handle an {@link Event} that reaches this object, considering that some other objects have been already visited.
     * @param e the Event that reaches this object
     * @param visited other objects already visited in the dispatch chain of this Event.
     */
    public void handleEvent(Event e, PSet<EventRecipient> visited);

}
