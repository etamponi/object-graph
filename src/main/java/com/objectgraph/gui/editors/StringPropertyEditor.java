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

import com.google.common.collect.Sets;
import com.objectgraph.core.RootedProperty;

import java.util.Collections;
import java.util.Set;

public class StringPropertyEditor extends TextFieldBasedPropertyEditor<String> {
    @Override
    protected String fromTextToModel(String text) {
        return text;
    }

    @Override
    protected String fromModelToText(String value) {
        return value == null ? "" : value;
    }

    @Override
    protected boolean isValid(String text) {
        return true;
    }

    @Override
    public boolean canEdit(RootedProperty model) {
        return model.getValueType(false).equals(String.class);
    }

    @Override
    public Set<Class<?>> getBaseEditableTypes() {
        return Collections.<Class<?>>singleton(String.class);
    }
}
