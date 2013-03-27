package com.objectgraph.gui.editors;/*
 * Copyright 2013 emanuele
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

 * You should have received a copy of the GNU General Public License
 * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.objectgraph.core.RootedProperty;

public class NumberEditor extends TextFieldBasedEditor<Object> {

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
    protected String fromModelToText(Object value) {
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

    @Override
    public boolean canEdit(RootedProperty model) {
        return true;
    }
}
