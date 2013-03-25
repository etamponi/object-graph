package com.objectgraph.core;

abstract class NodeHelper<N extends Node> {
	
	private N node;
	
	void setNode(N node) {
		this.node = node;
	}
	
	protected N getNode() {
		return node;
	}
	
	@SuppressWarnings("unchecked")
	protected <NN extends Node> NN getNode(Class<NN> type) {
		return (NN)node;
	}

}
