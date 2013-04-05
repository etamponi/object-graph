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

package com.objectgraph.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class ObjectNodeTest {

    private static class TestChild extends ObjectNode {
        @Property String s;
        @Property int i;
    }
    private static class TestBase extends ObjectNode {
        @Property Node child = new TestChild();
        @Property String name;

        public TestBase() {
            initialiseNode();
        }
    }

    @Test
    public void testAccessors() throws Exception {
        TestBase base = new TestBase();
        base.set("name", "test object");
        base.set("child.s", "test child");
        base.set("child.i", 100);

        assertEquals("test object", base.get("name"));
        assertEquals("test child", base.get("child.s"));
        assertEquals(100, base.get("child.i"));
    }

    @Test(expected = PropertyNotExistsException.class)
    public void testExceptionOnSet() throws Exception {
        TestBase base = new TestBase();
        base.set("invalidProperty", "Hello!");
    }

    @Test(expected = PropertyNotExistsException.class)
    public void testExceptionOnGet() throws Exception {
        TestBase base = new TestBase();
        base.get("invalidProperty");
    }

    @Test
    public void testGetProperties() throws Exception {
        TestBase base = new TestBase();
        assertEquals(Arrays.asList("child", "name"), base.getProperties());
        TestChild child = new TestChild();
        assertEquals(Arrays.asList("s", "i"), child.getProperties());
    }

    @Test
    public void testGetControlledProperties() throws Exception {
        TestBase base = new TestBase();

        Trigger trigger = mock(Trigger.class);
        when(trigger.getControlledPaths()).thenReturn(Arrays.asList("name"));
        when(trigger.isTriggeredBy(isA(Event.class))).thenReturn(false);
        when(trigger.getNode()).thenReturn(base);

        base.addTrigger(trigger);

        assertEquals(Arrays.asList("child"), base.getFreeProperties());
        assertEquals(Arrays.asList("name"), base.getControlledProperties());

        base.removeTrigger(trigger);

        assertTrue(base.getControlledProperties().isEmpty());
    }

    @Test
    public void testGetPropertyType() throws Exception {
        TestBase base = new TestBase();
        assertEquals(Node.class, base.getPropertyType("child", false));
        assertEquals(TestChild.class, base.getPropertyType("child", true));
        assertEquals(String.class, base.getPropertyType("name", false));
        assertEquals(null, base.getPropertyType("name", true));
        assertEquals(int.class, base.get("child", TestChild.class).getPropertyType("i", false));
    }

    public void testGetPossiblePropertyValues() throws Exception {

    }

    @Test
    public void testGetErrors() throws Exception {
        TestBase base = new TestBase();

        Error error = new Error(Error.ErrorLevel.WARNING, "Sample error");
        ErrorCheck check = mock(ErrorCheck.class);
        when(check.getNode()).thenReturn(base);
        when(check.getPath()).thenReturn("child.s");
        when(check.getError()).thenReturn(error);

        base.addErrorCheck(check);
        TestChild child = new TestChild();
        base.set("child", child);

        assertEquals(Arrays.asList(check), child.getErrorChecks("s"));

        Map<String,Set<Error>> errors = base.getErrors();
        assertEquals(Sets.newHashSet("child.s"), errors.keySet());
        assertEquals(Sets.newHashSet(error), errors.get("child.s"));

        errors = child.getErrors();
        assertTrue(errors.isEmpty());
    }

}
