package com.objectgraph.pluginsystem;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import com.objectgraph.core.Constraint;
import com.objectgraph.core.Node;
import com.objectgraph.core.RootedProperty;
import com.objectgraph.gui.PropertyEditor;
import com.objectgraph.pluginsystem.exceptions.PluginManagerAlreadyInitializedException;
import com.objectgraph.pluginsystem.exceptions.PluginManagerNotInitializedException;
import org.reflections.Reflections;

import com.objectgraph.utils.ClassUtils;


public class PluginManager {
	
	private static PluginConfiguration configuration = null;
	private static ClassLoader classLoader = PluginManager.class.getClassLoader();
	private static Reflections internal = null;

	private PluginManager() {
		// you should not instantiate a PluginManager
	}
	
	public static void initialise(PluginConfiguration conf) {
        if (configuration != null)
            throw new PluginManagerAlreadyInitializedException();

		configuration = Node.getKryo().copy(conf);
		prepare();
	}
	
	public static PluginConfiguration getConfiguration() {
        if (configuration == null)
            throw new PluginManagerNotInitializedException();

		return Node.getKryo().copy(configuration);
	}
	
	public static ClassLoader getClassLoader() {
		return classLoader;
	}
	
	private static void prepare() {
		Set<URL> urls = new HashSet<>();
		for(File file: configuration.libraries) {
			if (file == null || !file.exists())
				continue;
			try {
				URL url = file.toURI().toURL();
				urls.add(url);
			} catch (MalformedURLException e) {
                // Do not do anything
            }
		}
		classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
		Set<String> packages = new HashSet<>();
		for(String p: configuration.packages) {
			if (p != null)
				packages.add(p);
		}
		internal = new Reflections(classLoader, packages.toArray(new String[packages.size()]));
	}
	
	public static <T> List<T> getImplementations(Class<T> baseType, List<Constraint<?,?>> constraints) {
		List<T> ret = new ArrayList<>();
		
		Set<Class<? extends T>> types = internal.getSubTypesOf(baseType);
		types.add(baseType);
		
		for(Class<? extends T> type: types) {
			if (ClassUtils.isImplementation(type)) {
				try {
					T obj = type.newInstance();
					ret.add(obj);
				} catch (InstantiationException | IllegalAccessException e) {
                    // Do not do anything: the class does not have an empty constructor
                }
			}
		}

        for(Constraint<?,?> c: constraints)
            c.filter((List) ret);
		
		Collections.sort(ret, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
			}
		});
		
		return ret;
	}

    public static PropertyEditor getBestEditor(RootedProperty model) {
        return getBestEditor(model, false);
    }

    private static final Map<Class<?>, Class<PropertyEditor>> cachedEditors = new HashMap<>();
    public static PropertyEditor getBestEditor(RootedProperty model, boolean attach) {
        Class<?> valueType = model.getValueType(true);
        if (cachedEditors.containsKey(valueType)) try {
            PropertyEditor cached = cachedEditors.get(valueType).newInstance();
            if (cached.canEdit(model))
                return cached;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }

        List<PropertyEditor> editors = getImplementations(PropertyEditor.class, Collections.<Constraint<?,?>>emptyList());
        Iterator<PropertyEditor> it = editors.iterator();
        PropertyEditor best = null;
        while(it.hasNext()) {
            PropertyEditor e = it.next();
            if (e.canEdit(model))
                best = updateBestEditor(valueType, best, e);
        }

        cachedEditors.put(valueType, (Class<PropertyEditor>)best.getClass());

        if (attach)
            return best.attach(model);
        else
            return best;
    }

    private static PropertyEditor updateBestEditor(Class<?> valueType, PropertyEditor current, PropertyEditor candidate) {
        if (current == null)
            return candidate;
        if (distance(valueType, current.getBaseEditableType()) > distance(valueType, candidate.getBaseEditableType()))
            return candidate;
        else
            return current;
    }

    private static final Map<Class<?>, Map<Class<?>, Double>> distances = new HashMap<>();
    private static double distance(Class<?> dst, Class<?> src) {
        if (!distances.containsKey(src)) {
            distances.put(src, new HashMap<Class<?>, Double>());
            distances.get(src).put(src, 0.0);
            distances.get(src).put(Object.class, 0.0);
        }
        if (distances.get(src).containsKey(dst))
            return distances.get(src).get(dst);
        else {
            double distance = 1 + distance(dst.getSuperclass(), src);
            for(Class<?> i: dst.getInterfaces()) {
                double current = 1.02 + distance(i, src);
                if (current < distance)
                    distance = current;
            }
            distances.get(src).put(dst, distance);
            return distance;
        }
    }

}
