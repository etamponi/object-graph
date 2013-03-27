package com.objectgraph.core;

import com.esotericsoftware.kryo.Kryo;
import com.objectgraph.core.eventtypes.changes.SetProperty;
import com.objectgraph.core.exceptions.NodeHelperAlreadyUsedException;
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
 * In turn, the following services are provided:<br/>
 * <br/>
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
public abstract class Node {

//	private final Map<Node, Set<String>> parentPaths = new WeakHashMap<>();

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
     * If you use the automatic construction for some properties that are themselves Nodes, they are assigned
     * without the use of the {@link #set(String, Object)} method, and then they need to be properly assigned.
     * This method does that for you, instead of assigning everything manually inside the constructor.
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
     * Adds a parent node to this.
     * <p/>
     * The event dispatch system works using the notion of "parent" Nodes: they are the Nodes that have a property
     * that points to this Node. Whenever an Event gets propagated to this Node, it gets dispatched to every parent
     * through a path defined by the {@code property} parameter.
     * <p/>
     * This method should be used with great care.
     *
     * @param parent   the parent Node
     * @param property the name of the property that connects the parent to this Node
     */
    protected void addParentPath(Node parent, String property) {
//		if (!parentPaths.containsKey(parent)) {
//			parentPaths.put(parent, new HashSet<String>());
//		}
//		parentPaths.get(parent).add(property);
        ParentRegistry.register(parent, property, this);
    }

    /**
     * Removes a previously defined parent Node
     * <p/>
     * See {@link #addParentPath(Node, String)} for a definition of parent Nodes.
     *
     * @param parent   the parent Node to remove from the parent list
     * @param property the name of the property that connects the parent to this Node
     */
    protected void removeParentPath(Node parent, String property) {
//		Set<String> properties = parentPaths.get(parent);
//		if (properties != null) {
//			properties.remove(property);
//			if (properties.isEmpty())
//				parentPaths.remove(parent);
//		}
        ParentRegistry.unregister(parent, property, this);
    }

    /**
     * Set the content of the property specified by the given path and fires a Change Event in case of success.
     *
     * @param path
     * @param value
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
        getControlledProperties(ret, "", HashTreePSet.<Node>singleton(this));
        return ret;
    }

    private void getControlledProperties(List<String> controlled, String prefixPath, PSet<Node> seen) {
        for (Trigger<?> t : triggers) {
            for (String path : t.getControlledPaths()) {
                if (PathUtils.isParent(prefixPath, path))
                    controlled.add(PathUtils.toLocalProperty(path));
            }
        }

        for (Node parent : getParentPaths().keySet()) {
            if (seen.contains(parent))
                continue;
            for (String path : getParentPaths().get(parent))
                parent.getControlledProperties(controlled, PathUtils.appendPath(path, prefixPath), seen.plus(parent));
        }
    }

    /**
     * @return
     */
    protected Map<Node, Set<String>> getParentPaths() {
        return ParentRegistry.getParentPaths(this);
    }

    /**
     * @param e
     */
    protected void fireEvent(Event e) {
        fireEvent(e, HashTreePSet.<Node>singleton(this));
    }

    private void fireEvent(Event e, PSet<Node> seen) {
        for (PropertyEditor editor : PropertyEditorRegistry.getAttachedEditors(this))
            if (editor.requiresViewUpdate(e))
                editor.updateView();

        for (Trigger<?> t : triggers)
            t.check(e);

        for (Node parent : getParentPaths().keySet()) {
            if (seen.contains(parent))
                continue;
            for (String path : getParentPaths().get(parent))
                parent.fireEvent(e.backPropagate(path), seen.plus(parent));
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
        getConstraints(property, list, HashTreePSet.<Node>empty());

        return PluginManager.getImplementations(getPropertyType(property, false), list);
    }

    /**
     * @param path
     * @param list
     * @param seen
     */
    private void getConstraints(String path, List<Constraint<?, ?>> list, PSet<Node> seen) {
        if (constraints.containsKey(path))
            list.addAll(constraints.get(path));

        for (Node parent : getParentPaths().keySet()) {
            if (seen.contains(parent))
                continue;
            for (String parentPath : getParentPaths().get(parent))
                parent.getConstraints(PathUtils.appendPath(parentPath, path), list, seen.plus(parent));
        }
    }

    /**
     * @param property
     * @param editor
     * @return
     */
    public <T extends PropertyEditor> T attachEditor(String property, T editor) {
        if (!hasProperty(property))
            throw new PropertyNotExistsException(new RootedProperty(this, property));
        PropertyEditorRegistry.register(editor, this, property);
        return editor;
    }

    /**
     * @param editor
     */
    public void detachEditor(PropertyEditor editor) {
        PropertyEditorRegistry.unregister(editor, this);
    }

    /**
     *
     */
    public void detachAllEditors() {
        PropertyEditorRegistry.unregisterAll(this);
    }

    public <T extends PropertyEditor> T getBestEditor(String property) {
        return (T) PluginManager.getBestEditor(new RootedProperty(this, property));
    }

}
