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


import com.google.common.base.Strings;
import com.objectgraph.core.*;
import com.objectgraph.core.Error;
import com.objectgraph.core.triggers.Assignment;
import com.objectgraph.gui.editors.ImplementationChooserPropertyEditor;
import com.objectgraph.gui.editors.StringPropertyEditor;
import com.objectgraph.pluginsystem.PluginConfiguration;
import com.objectgraph.pluginsystem.PluginManager;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Prova extends Application {
    private static ProvaNode node = new ProvaNode();

    public static class ProvaNode extends ObjectNode {
        @Property String s;
        @Property int i;
        @Property double d;
        @Property float f;
        @Property short sh;
        @Property boolean b;
        @Property Node node;
        @Property ListNode<Node> list = new ListNode<>(Node.class);

        public ProvaNode() {
            addConstraint(new Constraint<ProvaNode, Node>("node") {
                @Override
                public Error getError(Node element) {
                    if (Strings.nullToEmpty(getNode().s).equals("Automatic message")) {
                        if (element instanceof ProvaNode)
                            return null;
                        else
                            return new Error(Error.ErrorLevel.SEVERE, "should be an instance of ProvaNode");
                    } else
                        return null;
                }
            });
            addErrorCheck(new ErrorCheck<Node, Object>("i") {
                @Override
                public Error getError(Object value) {
                    if (value == 12)
                        return null;
                    else
                        return new Error(Error.ErrorLevel.WARNING, "value should be 12");
                }
            });
            initialiseNode();
        }
    }

    public static class ProvaParent extends ObjectNode {
        @Property
        protected String t = "Ciao";
        @Property
        protected ProvaNode child;

        public ProvaParent() {
            addTrigger(new Assignment("t", "child.s"));
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        PropertyEditor[] editors = {
                EditorManager.getBestEditor(node.getRootedProperty("s"), false, true),
                EditorManager.getBestEditor(node.getRootedProperty("node"), false, true),
                EditorManager.getBestEditor(node.getRootedProperty("i"), false, true),
                EditorManager.getBestEditor(node.getRootedProperty("d"), false, true),
                EditorManager.getBestEditor(node.getRootedProperty("f"), false, true),
                EditorManager.getBestEditor(node.getRootedProperty("sh"), false, true),
                EditorManager.getBestEditor(node.getRootedProperty("b"), false, true),
                EditorManager.getBestEditor(node.getRootedProperty("list"), false, true)
        };

        final VBox box = new VBox();
        Button test = new Button("Automatic");
        test.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                node.set("s", "Automatic message");
            }
        });
        box.getChildren().addAll(test);
        box.getChildren().addAll(editors);

        Scene scene = new Scene(box);
        scene.getStylesheets().add("com/objectgraph/gui/objectgraphgui.css");

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws Exception {
        PluginManager.initialise(new PluginConfiguration("com.objectgraph"));

        launch(args);
    }

}
