package com.objectgraph.core;

import java.util.List;


public abstract class Trigger<N extends Node> extends NodeHelper<N> {

	private boolean listening = true;
	
	public abstract List<String> getControlledPaths();
	
	protected abstract boolean isTriggeredBy(Event event);
	
	protected abstract void action(Event event);
	
	public final void check(Event event) {
		if (listening && isTriggeredBy(event)) {
			listening = false;
			action(event);
			listening = true;
		}
	}

}
