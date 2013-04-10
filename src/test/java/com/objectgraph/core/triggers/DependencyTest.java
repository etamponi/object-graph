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

package com.objectgraph.core.triggers;

import com.objectgraph.core.ObjectNode;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DependencyTest {
    private static class TestChild extends ObjectNode {
        @Property String text;
        @Property int value;
    }
    private static class TestNode extends ObjectNode {
        @Property TestChild child = new TestChild();
        @Property String string;
        @Property int number;

        public TestNode() {
            initialiseNode();
        }

        protected String computeText(String s, int n) {
            return n + " times " + s;
        }
    }

    private static class TestHelper {
        protected String computeString(String text, int value) {
            return text + ": " + value;
        }
        protected int computeNumber(String text, int value) {
            return text.length() + value;
        }
    }

    @Test
    public void testAction() throws Exception {
        TestNode node = new TestNode();

        Dependency trigger = new Dependency("child.text", "computeText", "string", "number");

        node.addTrigger(trigger);

        assertEquals(Arrays.asList("text"), node.child.getControlledProperties());

        node.set("string", "hello!");
        assertEquals("0 times hello!", node.get("child.text"));

        node.set("number", 10);
        assertEquals("10 times hello!", node.get("child.text"));

        node.set("child", new TestChild());
        assertEquals("10 times hello!", node.get("child.text"));
    }

    @Test
    public void testActionExternalObject() throws Exception {
        TestNode node = new TestNode();

        Dependency trigger1 = new Dependency("string", new TestHelper(), "computeString", "child.text", "child.value");
        Dependency trigger2 = new Dependency("number", new TestHelper(), "computeNumber", "child.text", "child.value");

        node.addTrigger(trigger1);
        node.addTrigger(trigger2);

        node.set("child.text", "Hello");
        assertEquals("Hello: 0", node.string);
        assertEquals(5, node.number);

        node.set("child.value", 10);
        assertEquals(15, node.number);
        assertEquals("Hello: 10", node.string);

        TestChild child = new TestChild();
        child.set("text", "Goodbye");
        child.set("value", 100);

        node.set("child", child);
        assertEquals(107, node.number);
        assertEquals("Goodbye: 100", node.string);

        assertEquals(Arrays.asList("child"), node.getFreeProperties());
        assertEquals(Arrays.asList("number", "string"), node.getControlledProperties());
    }
}
