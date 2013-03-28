package com.objectgraph.gui;

import com.objectgraph.core.Event;
import com.objectgraph.core.EventRecipient;
import com.objectgraph.core.RootedProperty;
import com.objectgraph.core.exceptions.EditorNotDetachedException;
import com.objectgraph.core.exceptions.InvalidModelForEditorException;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import org.pcollections.PSet;

import java.io.IOException;
import java.util.Set;

public abstract class PropertyEditor extends AnchorPane implements Initializable, EventRecipient {

    private RootedProperty model;
    private boolean listening = true;

    public PropertyEditor(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            loader.setController(this);
            Parent root = (Parent) loader.load();
            getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public PropertyEditor attach(RootedProperty model) {
        if (model == null) {
            detach();
            return this;
        }

        if (!canEdit(model)) {
            throw new InvalidModelForEditorException();
        }
        if (this.model != null) {
            throw new EditorNotDetachedException();
        }

        model.getRoot().addParentPath(this, "");
        this.model = model;

        updateView();

        return this;
    }

    public void detach() {
        if (model != null) {
            model.getRoot().removeParentPath(this, "");
        }
    }

    public abstract void updateModel();

    public abstract boolean requiresViewUpdate(Event event);

    public abstract void updateView();

    public abstract boolean canEdit(RootedProperty model);

    public abstract Set<Class<?>> getBaseEditableTypes();

    public RootedProperty getModel() {
        return model;
    }

    @Override
    public void handleEvent(Event e, PSet<EventRecipient> seen) {
        if (listening && requiresViewUpdate(e)) {
            listening = false;
            updateView();
            listening = true;
        }
    }

}
