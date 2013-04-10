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

package com.objectgraph.gui.editors;

import com.google.common.collect.Sets;

import java.util.Set;

public class NumberPropertyEditor extends TextFieldBasedPropertyEditor<Number> {

    @Override
    protected Number fromTextToModel(String text) {
        Class<?> type = getModel().getValueType(true);
        if (type == byte.class || type == Byte.class)
            return Byte.parseByte(text);
        if (type == short.class || type == Short.class)
            return Short.parseShort(text);
        if (type == int.class || type == Integer.class)
            return Integer.parseInt(text);
        if (type == long.class || type == Long.class)
            return Long.parseLong(text);
        if (type == float.class || type == Float.class)
            return Float.parseFloat(text);
        if (type == double.class || type == Double.class)
            return Double.parseDouble(text);
        return null; // cannot happen
    }

    @Override
    protected String fromModelToText(Number value) {
        return String.valueOf(value);
    }

    @Override
    protected boolean isValid(String text) {
        try {
            fromTextToModel(text);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static final Set<Class<?>> supported = Sets.<Class<?>>newHashSet(
            byte.class, Byte.class, short.class, Short.class, int.class, Integer.class,
            long.class, Long.class, float.class, Float.class, double.class, Double.class);

    @Override
    public boolean canEdit(Class<?> valueType) {
        return supported.contains(valueType);
    }

    @Override
    public Set<Class<?>> getBaseEditableTypes() {
        return supported;
    }
}
