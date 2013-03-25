package com.objectgraph.core.triggers;

import com.objectgraph.core.Event;
import com.objectgraph.core.Node;
import com.objectgraph.core.eventtypes.changes.Change;
import com.objectgraph.utils.PathUtils;

public class Cloning extends StaticControlledPathTrigger<Node> {
	
	private final String masterPath;
	
	public Cloning(String master, String... controlled) {
		super(controlled);
		this.masterPath = master;
	}

	@Override
	protected boolean isTriggeredBy(Event event) {
		if (event.getType() instanceof Change) {
			if (PathUtils.samePrefix(event.getPath(), masterPath))
				return true;
			
			for(String path: getControlledPaths())
				if (PathUtils.isPrefix(event.getPath(), path))
					return true;
			return false;
		} else {
			return false;
		}
	}

	@Override
	protected void action(Event event) {
		Object content = getNode().get(masterPath);
		if (PathUtils.samePrefix(event.getPath(), masterPath)) {
			for(String path: getControlledPaths()) {
				Object clone = Node.getKryo().copy(content);
				getNode().set(path, clone);
			}
		} else {
			for(String path: getControlledPaths()) {
				if (PathUtils.samePrefix(event.getPath(), path)) {
					Object clone = Node.getKryo().copy(content);
					getNode().set(path, clone);
				}
			}
		}
	}

}
