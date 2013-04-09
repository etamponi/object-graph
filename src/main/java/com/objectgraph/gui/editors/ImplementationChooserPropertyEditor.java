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

package com.objectgraph.gui.editors;

import com.objectgraph.core.Event;
import com.objectgraph.core.RootedProperty;
import com.objectgraph.core.SetProperty;
import com.objectgraph.gui.EditorManager;
import com.objectgraph.gui.PropertyEditor;
import com.objectgraph.utils.ClassUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class ImplementationChooserPropertyEditor extends PropertyEditor {

    @FXML
    private ComboBox<Object> implementationBox;
    @FXML
    private Button commitButton, cancelButton;
    @FXML
    private Button editButton;
    @FXML
    private Button loadButton;

    private List<?> implementations;
    private static final Object NULLCONTENT = new Object();

    private class ImplementationListCell extends ListCell<Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (item == NULLCONTENT || item == null || empty) {
                setText("<null>");
                item = null;
            } else {
                setText(item.getClass().getSimpleName());
            }

            if (getModel().getValue() == item) {
                setText(getText() + " (current)");
            }
        }
    }

    public ImplementationChooserPropertyEditor() {
        super("ImplementationChooserEditorView.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                updateModel();
            }
        });

        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                updateView();
            }
        });

        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // TODO loadButton
            }
        });

        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                updateModel();
                openSpecificEditor();
            }
        });

        implementationBox.setCellFactory(new Callback<ListView<Object>, ListCell<Object>>() {
            @Override
            public ListCell<Object> call(ListView<Object> list) {
                return new ImplementationListCell();
            }
        });

        implementationBox.setButtonCell(new ImplementationListCell());

        implementationBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observableValue, Object oldValue, Object newValue) {
                editButton.setDisable(newValue == NULLCONTENT);
            }
        });
    }

    private void openSpecificEditor() {
        EditorManager.openBestEditorStage(getModel(), true, true);
    }

    public void updateModel() {
        if (getModel() != null) {
            Object newValue = implementationBox.getValue();
            if (newValue == NULLCONTENT) {
                getModel().setValue(null);
            } else {
                getModel().setValue(newValue);
            }
        }
    }

    @Override
    public void updateView() {
        if (getModel() != null) {
            implementations = getModel().getPossibleValues();
            implementationBox.getItems().clear();
            Object current = getModel().getValue();
            if (current != null) {
                implementationBox.getItems().add(getModel().getValue());
            }
            implementationBox.getItems().add(NULLCONTENT);
            implementationBox.getItems().addAll(implementations);
            implementationBox.getSelectionModel().selectFirst();
        }
    }

    @Override
    public boolean requiresViewUpdate(Event event) {
        if (event.getType() instanceof SetProperty) {
            if (event.getPath().equals(getModel().getPath())) {
                if (implementationBox.getSelectionModel().getSelectedIndex() != 0) {
                    return true;
                }
                Object currentInView = implementationBox.getValue();
                currentInView = currentInView == NULLCONTENT ? null : currentInView;
                Object currentInModel = getModel().getValue();
                if (currentInView != currentInModel) {
                    return true;
                }
            }
        }
        List<?> possibleValues = getModel().getPossibleValues();
        if (possibleValues.size() != implementations.size()) {
            return true;
        }
        for (int i = 0; i < possibleValues.size(); i++) {
            if (!possibleValues.get(i).getClass().equals(implementations.get(i).getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canEdit(RootedProperty model) {
        return !ClassUtils.isConcrete(model.getValueType(false));
    }

    @Override
    public Set<Class<?>> getBaseEditableTypes() {
        return Collections.emptySet();
    }

}
