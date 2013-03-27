package com.objectgraph.core.eventtypes.changes;

import com.objectgraph.core.RootedProperty;

public class SetProperty extends Change {

    private final RootedProperty path;
    private final Object oldContent;
    private final Object newContent;

    public SetProperty(RootedProperty path, Object oldContent, Object newContent) {
        this.path = path;
        this.oldContent = oldContent;
        this.newContent = newContent;
    }

    public RootedProperty getPath() {
        return path;
    }

    public Object getOldValue() {
        return oldContent;
    }

    public Object getNewValue() {
        return newContent;
    }

}
