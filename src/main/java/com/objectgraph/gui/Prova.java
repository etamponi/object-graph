package com.objectgraph.gui;


import com.google.common.base.Strings;
import com.objectgraph.core.Constraint;
import com.objectgraph.core.MapNode;
import com.objectgraph.core.Node;
import com.objectgraph.core.ObjectNode;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Prova {
    private static ProvaNode node = new ProvaNode();

    public static class ProvaNode extends ObjectNode {
        @Property
        protected String s;
        @Property
        protected int i;
        @Property
        protected double d;
        @Property
        protected float f;
        @Property
        protected short sh;
        @Property
        protected boolean b;
        @Property
        protected Node node;

        public ProvaNode() {
            addConstraint(new Constraint<ProvaNode, Node>("node") {
                @Override
                protected String check(Node element) {
                    if (Strings.nullToEmpty(getNode().s).equals("Automatic message")) {
                        if (element instanceof MapNode)
                            return null;
                        else
                            return "should be an instance of MapNode";
                    } else
                        return null;
                }
            });
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

    public static class MyApp extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            PropertyEditor editor1 = new StringPropertyEditor().attach(node.getProperty("s"));
            PropertyEditor editor2 = new StringPropertyEditor().attach(node.getProperty("s"));
            PropertyEditor[] editors = {
                    PluginManager.getBestEditor(node.getProperty("i"), true),
                    PluginManager.getBestEditor(node.getProperty("d"), true),
                    PluginManager.getBestEditor(node.getProperty("f"), true),
                    PluginManager.getBestEditor(node.getProperty("sh"), true),
                    PluginManager.getBestEditor(node.getProperty("b"), true),
            };
            PropertyEditor editor3 = new ImplementationChooserPropertyEditor().attach(node.getProperty("node"));

            VBox box = new VBox();
            Button test = new Button("Automatic");
            test.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    node.set("s", "Automatic message");
                }
            });
            box.getChildren().addAll(editor1, editor2, editor3, test);
            box.getChildren().addAll(editors);

            Scene scene = new Scene(box);
            scene.getStylesheets().add("com/objectgraph/gui/objectgraphgui.css");

            stage.setScene(scene);
            stage.show();
        }
    }

    public static void provaParent(ProvaNode node) {
        new ProvaParent().set("child", node);
    }

    public static void main(String[] args) {
        PluginManager.initialise(new PluginConfiguration("com.objectgraph"));
        Application.launch(MyApp.class);
        System.out.println(node.getErrors());
        System.out.println(node.b);
        int i = 1;
        ProvaNode node = new ProvaNode();
        provaParent(node);
        while (node.getFreeProperties().size() != node.getProperties().size()) {
            System.out.println(i++);
            if (i > 2)
                System.gc();
        }

        System.out.println("Finished!");
    }

}
