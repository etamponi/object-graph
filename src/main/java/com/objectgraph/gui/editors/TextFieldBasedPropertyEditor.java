package com.objectgraph.gui.editors;

import com.objectgraph.core.Event;
import com.objectgraph.core.eventtypes.changes.SetProperty;
import com.objectgraph.gui.PropertyEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class TextFieldBasedPropertyEditor<T> extends PropertyEditor<T> {

    @FXML
    protected TextField textField;
    @FXML
    protected Button commitButton;
    @FXML
    protected Button cancelButton;

    public TextFieldBasedPropertyEditor() {
        super("TextFieldBasedEditorView.fxml");
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
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                updateStyle(newValue);
            }
        });
    }

    private void updateStyle(String text) {
        textField.getStyleClass().removeAll("text-field-editor-changed", "text-field-editor-invalid", "text-field-editor");
        if (getModel() != null) {
            if (isValid(text)) {
                String curValue = fromModelToText((T)getModel().getValue());
                if (!text.equals(curValue))
                    textField.getStyleClass().add("text-field-editor-changed");
                else
                    textField.getStyleClass().add("text-field-editor");
            } else {
                textField.getStyleClass().add("text-field-editor-invalid");
            }
        }
    }

    @Override
    public void updateModel() {
        if (getModel() != null && isValid(textField.getText())) {
            getModel().setValue(fromTextToModel(textField.getText()));
            updateStyle(textField.getText());
        }
    }

    protected abstract T fromTextToModel(String text);

    protected abstract String fromModelToText(T value);

    protected abstract boolean isValid(String text);

    @Override
    public void updateView() {
        if (getModel() != null) {
            textField.setText(fromModelToText((T)getModel().getValue()));
        }
    }

    @Override
    public boolean requiresViewUpdate(Event event) {
        if (event.getType() instanceof SetProperty) {
            if (event.getPath().equals(getModel().getPath())) {
                if (!event.getType(SetProperty.class).getNewValue().equals(textField.getText()))
                    return true;
            }
        }
        return false;
    }

}
