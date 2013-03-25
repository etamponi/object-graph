package com.objectgraph.core.triggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.objectgraph.core.Node;
import com.objectgraph.core.Trigger;

public abstract class StaticControlledPathTrigger<N extends Node> extends Trigger<N> {
	
	private final List<String> controlledPaths;
	
	public StaticControlledPathTrigger(String... controlled) {
		controlledPaths = new ArrayList<>(Arrays.asList(controlled));
	}

	@Override
	public List<String> getControlledPaths() {
		return Collections.unmodifiableList(controlledPaths);
	}

}
