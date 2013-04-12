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

import com.esotericsoftware.reflectasm.FieldAccess;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;

/**
 * A Node whose properties are its fields with the {@link Property} annotation.
 * <p/>
 * This is the main implementation of the {@link Node} class. By extending this class, you define as <i>properties</i>
 * those fields of the subclass which are annotated with the tag @Property. You can define any field as a property, but
 * it cannot be private.
 * <p/>
 * Let's explain with some examples:
 * <pre>
 *     public class MyChild extends ObjectNode {
 *         {@literal @}Property double percent;
 *         {@literal @}Property String description;
 *
 *         private String notProperty;
 *          :
 *          :
 *     }
 *
 *     public class MyNode extends ObjectNode {
 *         Property String text;
 *         {@literal @}Property int number;
 *         {@literal @}Property MyChild child;
 *          :
 *          :
 *     }
 *
 *     public static void main(String... args) {
 *         MyNode obj = new MyNode();
 *         obj.set("text", "Hello, World");
 *         obj.set("number", 123);
 *         obj.set("child", new MyChild());
 *         obj.set("child.percent", 0.34);
 *         obj.set("child.description", "This is a MyChild object");
 *
 *         MyChild child = obj.get("child");
 *         child.set("notProperty", "ERROR!"); // This will raise a RuntimeException
 *          :
 *          :
 *     }
 * </pre>
 * You can define {@link Trigger}s in the constructor or attach them to an instance at any time. Consider that the Trigger
 * will not be checked when registered, but only if a triggering {@link Event} reaches the Node <i>after</i> the Trigger
 * has been registered.
 * <p/>
 * You can also define {@link ErrorCheck}s and {@link com.objectgraph.core.errorchecks.Constraint}s. These are particularly useful when coupled with a GUI
 * or if you have a complex batch execution or a separate configuration file.
 * <p/>
 * For example:
 * <pre>
 *     public class MyNode extends Node {
 *         {@literal @}Property String text;
 *         {@literal @}Property int value;
 *
 *         public MyNode() {
 *             addTrigger(new Dependency("text", "toText", "value"));
 *             addErrorCheck(new RangeCheck("value", 200, 300));
 *         }
 *
 *         protected String toText(int value) {
 *             return "The value property is " + value;
 *         }
 *     }
 *
 *     public static void main(String... args) {
 *         MyNode obj = new MyNode();
 *         obj.set("value", 123);
 *         System.out.println(obj.get("text")); // output: "The value property is 123"
 *
 *         Map{@literal <String, Set<Error>>} errors = node.getErrors();
 *         if (!errors.isEmpty) {
 *             for(String property: errors.keySet())
 *                 System.out.println(property + ": " + errors.get(property).getMessage());
 *         }
 *     }
 * </pre>
 */
public abstract class ObjectNode extends Node {

    /**
     * The annotation to use to add a property to an ObjectNode
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Property {}

    private static class PropertyAccess {

        private static final Map<Class<? extends ObjectNode>, PropertyAccess> instances = new HashMap<>();

        private final List<String> properties = new ArrayList<>();
        private final Map<String, Class<?>> propertyTypes = new HashMap<>();

        private PropertyAccess(Class<? extends ObjectNode> type) {
            recursivelyFindProperties(type);
        }

        private void recursivelyFindProperties(Class<?> type) {
            if (type.equals(ObjectNode.class))
                return;
            recursivelyFindProperties(type.getSuperclass());
            findDeclaredProperties(type);
        }

        private void findDeclaredProperties(Class<?> type) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Property.class)) {
                    properties.add(field.getName());
                    propertyTypes.put(field.getName(), field.getType());
                }
            }
        }

        public List<String> getProperties() {
            return Collections.unmodifiableList(properties);
        }

        public Class<?> getDeclaredPropertyType(String property) {
            return propertyTypes.get(property);
        }

        public static PropertyAccess get(Class<? extends ObjectNode> type) {
            if (!instances.containsKey(type)) {
                instances.put(type, new PropertyAccess(type));
            }
            return instances.get(type);
        }

    }

    @Override
    protected void setLocal(String property, Object content) {
        FieldAccess access = FieldAccess.get(getClass());
        access.set(this, property, content);
    }

    @Override
    protected <T> T getLocal(String property) {
        FieldAccess access = FieldAccess.get(getClass());
        return (T) access.get(this, property);
    }

    @Override
    public List<String> getProperties() {
        return PropertyAccess.get(getClass()).getProperties();
    }

    @Override
    protected Class<?> getDeclaredPropertyType(String property) {
        return PropertyAccess.get(getClass()).getDeclaredPropertyType(property);
    }

}
