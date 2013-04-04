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

package com.objectgraph.utils;

import java.lang.reflect.Modifier;

public final class ClassUtils {

    private ClassUtils() {
    }

    public static boolean isImplementation(Class<?> type) {
        return isConcrete(type)
                && Modifier.isPublic(type.getModifiers())
                && (type.getEnclosingClass() == null || Modifier.isStatic(type.getModifiers()));
    }

    public static boolean isConcrete(Class<?> type) {
        return type.isPrimitive() ||
                (!Modifier.isAbstract(type.getModifiers()) && !Modifier.isInterface(type.getModifiers()));
    }

}
