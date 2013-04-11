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

import com.objectgraph.core.Event;
import com.objectgraph.core.ObjectNode;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AssignmentTest {

    private static class TestChild extends ObjectNode {
        @Property String string;
    }

    private static class TestNode extends ObjectNode {
        @Property String string;
        @Property TestChild childMaster;
        @Property TestChild childSlave;
    }

    @Test
    public void testAction() throws Exception {
        TestNode node = new TestNode();

        Assignment trigger1 = spy(new Assignment("string", "childMaster.string"));
        node.addTrigger(trigger1);

        Assignment trigger2 = spy(new Assignment("childMaster", "childSlave"));
        node.addTrigger(trigger2);

        assertEquals(Arrays.asList("childSlave"), node.getControlledProperties());

        node.set("childMaster", new TestChild());

        assertEquals(node.childMaster, node.childSlave);
        assertEquals(node.string, node.childMaster.string);

        node.set("string", "Hello!");

        assertEquals(node.string, node.childMaster.string);
        assertEquals(node.string, node.childSlave.string);

        verify(trigger1, times(2)).action(isA(Event.class));
        verify(trigger2, times(1)).action(isA(Event.class));
    }
}
