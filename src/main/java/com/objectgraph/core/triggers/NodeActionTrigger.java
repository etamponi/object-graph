package com.objectgraph.core.triggers;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.objectgraph.core.Event;
import com.objectgraph.core.Node;
import com.objectgraph.core.eventtypes.changes.Change;

public class NodeActionTrigger<N extends Node> extends StaticControlledPathTrigger<N> {

    private final String handlerMethod;

    public NodeActionTrigger(String handlerMethod, String... controlled) {
        super(controlled);
        this.handlerMethod = handlerMethod;
    }

    @Override
    protected boolean isTriggeredBy(Event event) {
        return (event.getType() instanceof Change);
    }

    @Override
    protected void action(Event event) {
        MethodAccess access = MethodAccess.get(getNode().getClass());
        access.invoke(getNode(), handlerMethod);
    }

}
