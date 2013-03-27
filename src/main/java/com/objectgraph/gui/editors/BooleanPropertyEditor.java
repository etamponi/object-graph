/*
 * Copyright 2013 emanuele
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

 * You should have received a copy of the GNU General Public License
 * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.objectgraph.gui.editors;

import com.objectgraph.core.Event;
import com.objectgraph.core.RootedProperty;
import com.objectgraph.gui.PropertyEditor;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.ResourceBundle;

public class BooleanPropertyEditor extends PropertyEditor<Object> {

    @FXML
    private CheckBox checkBox;

    public BooleanPropertyEditor() {
        super("BooleanEditorView.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        checkBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                updateModel();
            }
        });
    }

    @Override
    public void updateModel() {
        if (getModel() != null) {
            getModel().setValue(checkBox.isSelected());
        }
    }

    @Override
    public void updateView() {
        if (getModel() != null) {
            checkBox.setSelected(getModel().getValue(boolean.class));
        }
    }

    @Override
    public boolean requiresViewUpdate(Event event) {
        return true;
    }

    @Override
    public boolean canEdit(RootedProperty model) {
        Class<?> type = model.getValueType(true);
        return type == boolean.class || type == Boolean.class;
    }
}
