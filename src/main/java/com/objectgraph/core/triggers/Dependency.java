package com.objectgraph.core.triggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.objectgraph.core.Event;
import com.objectgraph.core.Node;
import com.objectgraph.core.eventtypes.changes.Change;
import com.objectgraph.utils.PathUtils;

public class Dependency extends StaticControlledPathTrigger<Node> {
	
	private final String handlerMethod;
	private final String controlledPath;
	private final List<String> parameterPaths;
    private final Object handlerObject;

    public Dependency(String controlled, String handlerMethod, String... parameters) {
		super(controlled);
		this.controlledPath = controlled;
		this.handlerMethod = handlerMethod;
        this.handlerObject = null;
		this.parameterPaths = new ArrayList<>(Arrays.asList(parameters));
	}

    public Dependency(String controlled, Object handlerObject, String handlerMethod, String... parameters) {
        super(controlled);
        this.controlledPath = controlled;
        this.handlerMethod = handlerMethod;
        this.handlerObject = handlerObject;
        this.parameterPaths = new ArrayList<>(Arrays.asList(parameters));
        if (handlerObject != null) {
            // TODO Add checks if possible
        }
    }

	@Override
	protected boolean isTriggeredBy(Event event) {
		if (event.getType() instanceof Change) {
			if (PathUtils.isPrefix(event.getPath(), controlledPath))
				return true;
			
			for (String path: parameterPaths)
				if (PathUtils.samePrefix(event.getPath(), path))
					return true;
			
			return false;
		} else {
			return false;
		}
	}

	@Override
	protected void action(Event event) {
        Object handler = handlerObject == null ? getNode() : handlerObject;
		MethodAccess access = MethodAccess.get(handler.getClass());
		Object[] params = new Object[parameterPaths.size()];
		int i = 0;
		for(String path: parameterPaths)
			params[i++] = getNode().get(path);
		getNode().set(controlledPath, access.invoke(handler, handlerMethod, params));
	}

}
