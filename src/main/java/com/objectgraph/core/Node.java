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

import com.esotericsoftware.kryo.Kryo;
import com.objectgraph.core.eventtypes.changes.SetProperty;
import com.objectgraph.core.exceptions.MalformedPathException;
import com.objectgraph.core.exceptions.PropertyNotExistsException;
import com.objectgraph.pluginsystem.PluginManager;
import com.objectgraph.utils.PathUtils;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.*;

/**
 * Main class of the Object-Graph Framework, provides event handling, error checking and more.
 * <p/>
 * This class provides various facilities to program in a dynamic and event-driven way
 * using standard Java objects, and provide easy access from UI. Subclasses of Node must implement:
 * <ul>
 * <li>A method that returns the names of the "local properties", through {@link #getProperties()}</li>
 * <li>Methods to get/set a local property: {@link #getLocal(String)}, {@link #setLocal(String, Object)}</li>
 * <li>A method to know the declared type for each property, {@link #getDeclaredPropertyType(String)}</li>
 * </ul>
 * <p/>
 * In turn, the following services are provided:
 * <dl>
 * <dt>Automatic dispatch of events through the graph defined by Node properties</dt>
 * <dd>Suppose that a Node, say X, has a property {@code "a"} which in turn references a Node Y with a property
 * {@code "b"}. If b is changed by using {@link #set(String, Object)}, an event is fired and gets propagated to Y
 * through the path "Y.b" and then to X through the path Y.a.b; any number of Triggers can be attached to
 * each Node, which are activated by a matching event. See below and {@link #addTrigger(Trigger)}.</dd>
 * <p/>
 * <dt>Event handling made easy</dt>
 * <dd>For each Event that gets propagated to this Node, each registered Trigger is requested to check if an action is
 * required for that event. For example, if you want to bind a property, say {@code c}, to a transformation
 * of two other properties, say {@code a, b}, you can do this by putting the following line in the constructor:
 * <pre>
 * public NodeType() {
 *       :
 *       :
 *     addTrigger(new Dependency("c", "transform", "a", "b"));
 *       :
 *       :
 * }
 * </pre>
 * together with a protected or public method {@code transform()} that takes two arguments of the same type of a and b
 * and that returns the same type as c.</dd>
 * <p/>
 * <dt>Constrained assignments</dt> <dd>TODO describe constrained assignments</dd>
 * <p/>
 * <dt>Runtime error checking</dt> <dd>When using almost any kind of UI, the user of the interface
 * will enter inconsistent or wrong values for some property. In those cases, you don't want that an exception
 * stops the execution of the UI: instead, it would be great to show an error message with an useful description
 * of the problem. In any cases, if a Node is requested to achieve a computation, a first step would be to check
 * if there are some Errors in it and in this case provide the user a notification and stop the computation
 * until the problems are fixed. This can be obtained using {@link #addErrorCheck(ErrorCheck)}.</dd>
 * </dl>
 *
 * @author Emanuele Tamponi
 */
public abstract class Node implements EventRecipient {

    private final Set<Trigger<?>> triggers = new HashSet<>();

    private final Set<ErrorCheck<?>> errorChecks = new HashSet<>();

    private final Map<String, Set<Constraint<?, ?>>> constraints = new HashMap<>();

    private final static Kryo kryo;

    static {
        kryo = new Kryo() {
            InstantiatorStrategy s = new StdInstantiatorStrategy();

            @Override
            protected ObjectInstantiator newInstantiator(final Class type) {
                if (Node.class.isAssignableFrom(type))
                    return s.newInstantiatorOf(type);
                else
                    return super.newInstantiator(type);
            }
        };
        kryo.addDefaultSerializer(Node.class, NodeSerializer.class);
        kryo.addDefaultSerializer(ListNode.class, NodeSerializer.class);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * A customised instance of Kryo for serialization and cloning.
     * <p/>
     * This Kryo instance has been set up to work nicely with Nodes: you should use it whenever you
     * need to serialize or clone a Node.
     *
     * @return <strong>The</strong> Kryo instance that should be used when working with Nodes
     */
    public static Kryo getKryo() {
        kryo.setClassLoader(PluginManager.getClassLoader());
        return kryo;
    }

    /**
     * Performs some tweaks on properties.
     * <p/>
     * You should call this method at the end of the constructor of every subclass, to assure that every property gets
     * correctly initialized. What this method does, is to register properties that may have been initialized outside
     * the constructor. For example, supposing that {@code Child} is a {@link Node} implementation:
     * <pre>
     *     public MyNode extends ObjectNode {
     *         @Property
     *         protected Child child = new Child(); // This is initialized outside the constructor, without set()
     *     }
     * </pre>
     * in this case, as you see, the property MyNode.child is not initialised using the {@link #set(String, Object)}
     * method. By calling {@code initialiseNode()} at the end of the constructor, every such property get correctly
     * set.
     *
     */
    protected void initialiseNode() {
        for (String property : getProperties()) {
            if (get(property) instanceof Node && !get(property, Node.class).getParentPaths().containsKey(this)) {
                Object value = get(property);
                setLocal(property, null);
                set(property, value);
            }
        }
    }

    /**
     * Adds an {@link EventRecipient} parent to this Node.
     * <p/>
     * The event dispatch system works using the notion of "parent" EventRecipients: whenever an Event gets propagated
     * to this Node, it gets dispatched to every parent through a path defined by the {@code property} parameter.
     * <p/>
     * This method should be used with great care, as it is used internally by {@link #set(String, Object)} and other
     * methods, and you should not need to use it directly.
     *
     * @param parent   the parent EventRecipient
     * @param property the name of the property that connects the parent to this Node
     */
    public void addParentPath(EventRecipient parent, String property) {
        ParentRegistry.register(parent, property, this);
    }

    /**
     * Removes a previously defined parent EventRecipient
     * <p/>
     * See {@link #addParentPath(EventRecipient, String)} for a definition of parents.
     *
     * @param parent   the parent EventRecipient to remove from the parent list
     * @param property the name of the property that connects the parent to this Node
     */
    public void removeParentPath(EventRecipient parent, String property) {
        ParentRegistry.unregister(parent, property, this);
    }

    /**
     * Set the content of the property specified by the given path and fires a SetProperty event in case of success.
     * <p/>
     * You should use this method to set Node properties (either directly, or wrapped in a standard setter method). It
     * is connected with the event system, so that once the property has been set, a new {@link Event} with type
     * {@link SetProperty} {@link EventType} gets fired and propagates through the graph defined by parents paths.
     * <p/>
     * This method can be used to set either a <i>local</i> property, or a <i>nested</i> property, that is, a property
     * that belongs to a Node which is in turn a property of this Node (the path can be arbitrarly deep).
     * <p/>
     * To show how you can use this method, consider the following example:
     * <pre>
     *     public class MyChild extends ObjectNode {
     *         @Property
     *         protected String s;
     *          :
     *          :
     *     }
     *
     *     public class MyNode extends ObjectNode {
     *         @Property
     *         protected MyChild child;
     *          :
     *          :
     *     }
     *
     *     public static void main(String... args) {
     *         MyNode node = new MyNode();
     *         node.set("child.s", "Hello, World!");
     *     }
     * </pre>
     *
     * @param path the named path to the property that should be set
     * @param value the new value of the property
     */
    public void set(String path, Object value) {
        if (path.isEmpty())
            throw new MalformedPathException(new RootedProperty(this, path), "Empty path cannot be set.");

        int firstSplit = path.indexOf('.');
        if (firstSplit < 0) {
            if (!hasProperty(path))
                throw new PropertyNotExistsException(new RootedProperty(this, path));

            Object oldValue = getLocal(path);
            if (oldValue != value) {
                if (oldValue instanceof Node) {
                    ((Node) oldValue).removeParentPath(this, path);
                }

                setLocal(path, value);

                if (value instanceof Node) {
                    ((Node) value).addParentPath(this, path);
                }

                fireEvent(new Event(path, new SetProperty(new RootedProperty(this, path), oldValue, value)));
            }
        } else {
            String localProperty = path.substring(0, firstSplit);
            String remainingPath = path.substring(firstSplit + 1);
            Node local = getLocal(localProperty);
            if (local != null)
                local.set(remainingPath, value);
        }
    }

    /**
     * @param property
     * @param value
     */
    protected abstract void setLocal(String property, Object value);

    /**
     * @param path
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path, Class<T> type) {
        return (T) get(path);
    }

    /**
     * @param path
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        if (path.isEmpty())
            return (T) this;

        int firstSplit = path.indexOf('.');
        if (firstSplit < 0) {
            if (!hasProperty(path))
                throw new PropertyNotExistsException(new RootedProperty(this, path));

            return getLocal(path);
        } else {
            String localProperty = path.substring(0, firstSplit);
            String remainingPath = path.substring(firstSplit + 1);
            Node local = getLocal(localProperty);
            if (local != null)
                return local.get(remainingPath);
            else
                return null;
        }
    }

    /**
     * @param property
     * @return
     */
    protected abstract <T> T getLocal(String property);

    /**
     * @param property
     * @return
     */
    public boolean hasProperty(String property) {
        return getProperties().contains(property);
    }

    /**
     * @return
     */
    public abstract List<String> getProperties();

    /**
     * @return
     */
    public List<String> getFreeProperties() {
        List<String> ret = new ArrayList<>(getProperties());
        ret.removeAll(getControlledProperties());
        return ret;
    }

    /**
     * @return
     */
    public List<String> getControlledProperties() {
        List<String> ret = new ArrayList<>();
        getControlledProperties("", ret, HashTreePSet.<Node>singleton(this));
        return ret;
    }

    private void getControlledProperties(String prefixPath, List<String> controlled, PSet<Node> seen) {
        for (Trigger<?> t : triggers) {
            for (String path : t.getControlledPaths()) {
                if (PathUtils.isParent(prefixPath, path))
                    controlled.add(PathUtils.toLocalProperty(path));
            }
        }

        for (EventRecipient p : getParentPaths().keySet()) {
            if (p instanceof Node && !seen.contains(p)) {
                Node parent = (Node)p;
                for (String path : getParentPaths().get(parent))
                    parent.getControlledProperties(PathUtils.appendPath(path, prefixPath), controlled, seen.plus(parent));
            }

        }
    }

    /**
     * @return
     */
    public Map<EventRecipient, Set<String>> getParentPaths() {
        return ParentRegistry.getParentPaths(this);
    }

    /**
     * @param e
     */
    protected void fireEvent(Event e) {
        handleEvent(e, HashTreePSet.<EventRecipient>singleton(this));
    }

    @Override
    public void handleEvent(Event e, PSet<EventRecipient> seen) {
        for (Trigger<?> t : triggers)
            t.check(e);

        for (EventRecipient parent : getParentPaths().keySet()) {
            if (seen.contains(parent))
                continue;
            for (String path : getParentPaths().get(parent))
                parent.handleEvent(e.backPropagate(path), seen.plus(parent));
        }
    }

    /**
     * @param t
     */
    @SuppressWarnings("unchecked")
    public <N extends Node> void addTrigger(Trigger<N> t) {
        t.setNode((N)this);
        triggers.add(t);
    }

    /**
     * @param t
     */
    @SuppressWarnings("unchecked")
    public <N extends Node> void removeTrigger(Trigger<N> t) {
        if (triggers.remove(t))
            t.setNode(null);
    }

    /**
     * @param property
     * @param runtime
     * @return
     */
    public Class<?> getPropertyType(String property, boolean runtime) {
        if (!hasProperty(property))
            throw new PropertyNotExistsException(new RootedProperty(this, property));
        if (runtime) {
            Object content = getLocal(property);
            return content == null ? null : content.getClass();
        } else {
            return getDeclaredPropertyType(property);
        }
    }

    /**
     * @param property
     * @return
     */
    protected abstract Class<?> getDeclaredPropertyType(String property);

    /**
     * @param e
     */
    @SuppressWarnings("unchecked")
    public <N extends Node> void addErrorCheck(ErrorCheck<N> e) {
        e.setNode((N) this);
        errorChecks.add(e);
    }

    /**
     * @param e
     */
    public void removeErrorCheck(ErrorCheck<?> e) {
        if (errorChecks.remove(e))
            e.setNode(null);
    }

    /**
     * @return
     */
    public Map<String, Error> getErrors() {
        Map<String, Error> ret = new LinkedHashMap<>();
        getErrors(ret, "", new HashSet<Node>());
        return ret;
    }

    private void getErrors(Map<String, Error> errors, String path, Set<Node> seen) {
        if (seen.contains(this))
            return;
        seen.add(this);
        for (ErrorCheck<?> check : errorChecks) {
            Error error = check.getError();
            if (error != null) {
                errors.put(path, error);
            }
        }
        for (String property : getProperties()) {
            Object content = get(property);
            if (content != null && content instanceof Node)
                ((Node) content).getErrors(errors, PathUtils.appendPath(path, property), seen);
        }
    }

    /**
     * @param t
     */
    @SuppressWarnings("unchecked")
    public <N extends Node> void addConstraint(Constraint<N, ?> t) {
        t.setNode((N) this);
        if (!constraints.containsKey(t.getPath()))
            constraints.put(t.getPath(), new HashSet<Constraint<?, ?>>());

        constraints.get(t.getPath()).add(t);
        errorChecks.add(t);
    }

    /**
     * @param t
     */
    public void removeConstraint(Constraint<?, ?> t) {
        String path = t.getPath();
        if (constraints.containsKey(path) && constraints.get(path).remove(t)) {
            if (constraints.get(path).isEmpty())
                constraints.remove(path);
            errorChecks.remove(t);
            t.setNode(null);
        }
    }

    /**
     * @param property
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<?> getPossiblePropertyValues(String property) {
        if (!hasProperty(property))
            throw new PropertyNotExistsException(new RootedProperty(this, property));

        List<Constraint<?, ?>> list = new ArrayList<>();
        getConstraints(property, list, HashTreePSet.<EventRecipient>empty());

        return PluginManager.getImplementations(getPropertyType(property, false), list);
    }

    /**
     * @param path
     * @param list
     * @param seen
     */
    public void getConstraints(String path, List<Constraint<?, ?>> list, PSet<EventRecipient> seen) {
        if (constraints.containsKey(path))
            list.addAll(constraints.get(path));

        for (EventRecipient p : getParentPaths().keySet()) {
            if (p instanceof Node && !seen.contains(p)) {
                Node parent = (Node)p;
                for (String parentPath : getParentPaths().get(parent))
                    parent.getConstraints(PathUtils.appendPath(parentPath, path), list, seen.plus(parent));
            }
        }
    }

    public RootedProperty getProperty(String property) {
        RootedProperty ret = new RootedProperty(this, property);
        if (!hasProperty(property))
            throw new PropertyNotExistsException(ret);
        return ret;
    }

}
