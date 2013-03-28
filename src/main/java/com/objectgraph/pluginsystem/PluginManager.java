package com.objectgraph.pluginsystem;

import com.objectgraph.core.Constraint;
import com.objectgraph.core.Node;
import com.objectgraph.pluginsystem.exceptions.PluginManagerAlreadyInitializedException;
import com.objectgraph.pluginsystem.exceptions.PluginManagerNotInitializedException;
import com.objectgraph.utils.ClassUtils;
import org.reflections.Reflections;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


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
        for (File file : configuration.libraries) {
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
        for (String p : configuration.packages) {
            if (p != null)
                packages.add(p);
        }
        internal = new Reflections(classLoader, packages.toArray(new String[packages.size()]));
    }

    public static <T> List<T> getImplementations(Class<T> baseType, List<Constraint<?, ?>> constraints) {
        List<T> ret = new ArrayList<>();

        Set<Class<? extends T>> types = internal.getSubTypesOf(baseType);
        types.add(baseType);

        for (Class<? extends T> type : types) {
            if (ClassUtils.isImplementation(type)) {
                try {
                    T obj = type.newInstance();
                    ret.add(obj);
                } catch (InstantiationException | IllegalAccessException e) {
                    // Do not do anything: the class does not have an empty constructor
                }
            }
        }

        for (Constraint<?, ?> c : constraints)
            c.filter((List) ret);

        Collections.sort(ret, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
            }
        });

        return ret;
    }


}
