package com.objectgraph.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

class PropertyEditorRegistry {
	
	private static final Map<PropertyEditor, Node> registry = new WeakHashMap<>();
	
	static void register(PropertyEditor editor, Node node, String property) {
        RootedProperty model = new RootedProperty(node, property);
        editor.setModel(model);
		registry.put(editor, node);
	}
	
	static void unregister(PropertyEditor editor, Node node) {
		if (registry.containsKey(editor) && registry.get(editor) == node) {
			registry.remove(editor);
			editor.setModel(null);
		}
	}
	
	static void unregisterAll(Node node) {
		Iterator<Entry<PropertyEditor, Node>> it = registry.entrySet().iterator();
		while(it.hasNext()) {
			Entry<PropertyEditor, Node> entry = it.next();
			if (entry.getValue() == node) {
				entry.getKey().setModel(null);
				it.remove();
			}
		}
	}
	
	static Set<PropertyEditor> getAttachedEditors(Node node) {
		Set<PropertyEditor> ret = new HashSet<>();
		for(Entry<PropertyEditor, Node> entry: registry.entrySet()) {
			if (entry.getValue() == node)
				ret.add(entry.getKey());
		}
		return ret;
	}
	
}
