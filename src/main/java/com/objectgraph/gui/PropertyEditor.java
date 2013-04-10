/*
 * Copyright 2013 Emanuele Tamponi
 *
 * This file is part of object-graph.
 *
 * object-graph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * object-graph is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.objectgraph.gui;

import com.objectgraph.core.Event;
import com.objectgraph.core.EventRecipient;
import com.objectgraph.core.RootedProperty;
import javafx.application.Platform;
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
            model = null;
        }
    }

    public abstract boolean requiresViewUpdate(Event event);

    public abstract void updateView();

    public abstract boolean canEdit(RootedProperty model);

    public abstract Set<Class<?>> getBaseEditableTypes();

    public RootedProperty getModel() {
        return model;
    }

    @Override
    public void handleEvent(final Event e, PSet<EventRecipient> visited) {
        // An event can occur in a thread that is not the JavaFX GUI Thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (listening && requiresViewUpdate(e)) {
                    listening = false;
                    updateView();
                    listening = true;
                }
            }
        });
    }

}
