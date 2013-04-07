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

import com.objectgraph.core.ListChange.ListChangeType;

import java.util.ListIterator;

public class ListNodeIterator<E> implements ListIterator<E> {

    private final ListIterator<E> iterator;

    private final ListNode<E> list;

    ListNodeIterator(ListNode<E> list, ListIterator<E> iterator) {
        this.list = list;
        this.iterator = iterator;
    }

    @Override
    public void add(E element) {
        int index = iterator.nextIndex();
        iterator.add(element);
        if (element instanceof Node) {
            ((Node) element).addParentPath(list, String.valueOf(index));
        }

        for (index = index + 1; index < list.size(); index++) {
            E current = list.get(index);
            if (current instanceof Node) {
                String oldPath = String.valueOf(index - 1);
                String newPath = String.valueOf(index);
                ((Node) current).removeParentPath(list, oldPath);
                ((Node) current).addParentPath(list, newPath);
            }
        }

        list.updatePropertyList();

        list.fireEvent(new Event("", new ListChange(ListChangeType.ADD, list, element, iterator.nextIndex(), true)));
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return iterator.hasPrevious();
    }

    @Override
    public E next() {
        return iterator.next();
    }

    @Override
    public int nextIndex() {
        return iterator.nextIndex();
    }

    @Override
    public E previous() {
        return iterator.previous();
    }

    @Override
    public int previousIndex() {
        return iterator.previousIndex();
    }

    @Override
    public void remove() {
        int index = iterator.nextIndex() - 1;
        E element = list.get(index);
        iterator.remove();
        if (element instanceof Node) {
            ((Node) element).removeParentPath(list, String.valueOf(index));
        }

        for (int i = index; i < list.size(); i++) {
            E current = list.get(i);
            if (current instanceof Node) {
                String oldPath = String.valueOf(i + 1);
                String newPath = String.valueOf(i);
                ((Node) current).removeParentPath(list, oldPath);
                ((Node) current).addParentPath(list, newPath);
            }
        }

        list.updatePropertyList();

        list.fireEvent(new Event("", new ListChange(ListChangeType.REMOVE, list, element, index, true)));
    }

    @Override
    public void set(E element) {
        list.set(iterator.nextIndex() - 1, element);
    }

}
