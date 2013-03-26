package com.objectgraph.gui;

import com.objectgraph.core.PropertyEditor;
import com.objectgraph.core.RootedProperty;
import com.objectgraph.core.exceptions.EditorNotDetachedException;
import com.objectgraph.core.exceptions.InvalidModelForEditorException;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

public abstract class EditorPane<T> extends AnchorPane implements Initializable, PropertyEditor {

    private RootedProperty model;

    public EditorPane(String fxmlFile) {
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

    public void setModel(RootedProperty model) {
        if (model == null) {
            this.model = null;
            return;
        }

        if (!canEdit(model)) {
            throw new InvalidModelForEditorException();
        }
        if (this.model != null) {
            throw new EditorNotDetachedException();
        }
        this.model = model;
        updateView();
    }

    public RootedProperty getModel() {
        return model;
    }

    @Override
    public Class<?> getBaseEditableType() {
        return (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
