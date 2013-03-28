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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapNode<K, V> extends Node implements Map<K, V> {

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V get(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<K> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V put(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> other) {
        // TODO Auto-generated method stub

    }

    @Override
    public V remove(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Collection<V> values() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void setLocal(String property, Object content) {
        // TODO Auto-generated method stub

    }

    @Override
    protected <T> T getLocal(String property) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Class<?> getDeclaredPropertyType(String property) {
        // TODO Auto-generated method stub
        return null;
    }

}
