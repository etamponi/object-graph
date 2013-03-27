package com.objectgraph.core;

public interface PropertyEditor {

    public void setModel(RootedProperty model);

    public RootedProperty getModel();

    public void updateModel();

    public void updateView();

    public boolean requiresViewUpdate(Event event);

    public boolean canEdit(RootedProperty model);

    public Class<?> getBaseEditableType();

}
