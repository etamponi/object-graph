package com.objectgraph.core;

import com.objectgraph.core.exceptions.NodeHelperAlreadyUsedException;

public abstract class NodeHelper<N extends Node> {

    private N node;

    void setNode(N node) {
        if (this.node != null)
            throw new NodeHelperAlreadyUsedException(this, this.node);
        this.node = node;
    }

    protected N getNode() {
        return node;
    }

    @SuppressWarnings("unchecked")
    protected <NN extends Node> NN getNode(Class<NN> type) {
        return (NN) node;
    }

}
