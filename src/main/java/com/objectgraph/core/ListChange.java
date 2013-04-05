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

import java.util.Collections;
import java.util.List;

public class ListChange extends Change {

    public enum ListChangeType {
        ADD, REMOVE
    }

    private final ListChangeType changeType;
    private final ListNode<?> list;
    private final List<?> elements;
    private final List<Integer> indices;
    private final boolean indirect;

    public <E> ListChange(ListChangeType changeType, ListNode<E> list, E element, int index) {
        this(changeType, list, element, index, false);
    }

    public <E> ListChange(ListChangeType changeType, ListNode<E> list, E element, int index, boolean indirect) {
        this.changeType = changeType;
        this.list = list;
        this.elements = Collections.unmodifiableList(Collections.singletonList(element));
        this.indices = Collections.unmodifiableList(Collections.singletonList(index));
        this.indirect = indirect;
    }

    public <E> ListChange(ListChangeType changeType, ListNode<E> list, List<E> elements, List<Integer> indices) {
        this.changeType = changeType;
        this.list = list;
        this.elements = Collections.unmodifiableList(elements);
        this.indices = Collections.unmodifiableList(indices);
        this.indirect = false;
    }

    public ListChangeType getChangeType() {
        return changeType;
    }

    public ListNode<?> getList() {
        return list;
    }

    public List<?> getElements() {
        return elements;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public boolean isIndirect() {
        return indirect;
    }

}
