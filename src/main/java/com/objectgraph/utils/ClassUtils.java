package com.objectgraph.utils;

import java.lang.reflect.Modifier;

public class ClassUtils {

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
