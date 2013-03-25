package com.objectgraph.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.reflectasm.FieldAccess;

public abstract class ObjectNode extends Node {
	
	/**
	 * 
	 * @author Emanuele
	 *
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
			for(Field field: type.getDeclaredFields()) {
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
		try {
		access.set(this, property, content);
		} catch (IllegalAccessError err) {
			System.out.println("OH");
		}
	}

	@Override
	protected <T> T getLocal(String property) {
		FieldAccess access = FieldAccess.get(getClass());
		return (T)access.get(this, property);
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
