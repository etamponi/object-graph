package com.objectgraph;

import com.objectgraph.core.ObjectNode;
import com.objectgraph.core.triggers.Dependency;

/**
 * Created with IntelliJ IDEA.
 * User: emanuele
 * Date: 25/03/13
 * Time: 22.15
 * To change this template use File | Settings | File Templates.
 */
public class Prova {

    public static class Child extends ObjectNode {
        @Property
        protected String s;
    }

    public static class Example extends ObjectNode {

        @Property
        protected String a;
        @Property
        protected String b;
        @Property
        protected String c;
        @Property
        protected String d;
        @Property
        protected Child child = new Child();

        public Example() {
            addTrigger(new Dependency("c", "join", "a", "b"));

            addTrigger(new Dependency("d", "join", "a", "child.s"));

            initialiseNode();
        }

        protected String join(String s1, String s2) {
            return s1 + " " + s2;
        }

        public String getC() {
            return c;
        }

        public void setA(String a) {
            set("a", a);
        }

    }

    public static void main(String... args) {
        Example obj = new Example();
        obj.set("a", "Ciao");
        obj.set("b", "a tutti");
        System.out.println(obj.getC());

        obj.set("b", "Andrea");
        System.out.println(obj.getC());

        obj.get("child", Child.class).set("s", "Pippo");

        Child c = obj.get("child");

        c.set("s", "Geronimo");

        System.out.println(obj.get("d"));
    }

molto molto grande

}
