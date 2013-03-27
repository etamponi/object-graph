package com.objectgraph.core;

public class Event {

    private final String path;

    private final EventType type;

    public Event(String path, EventType type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public EventType getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <ET extends EventType> ET getType(Class<ET> cls) {
        return (ET) type;
    }

    public Event backPropagate(String parent) {
        if (path.isEmpty())
            return new Event(parent, type);
        else
            return new Event(parent + "." + path, type);
    }

}
