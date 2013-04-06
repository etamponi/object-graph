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

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ListNodeTest2 {

    private static class TestElement extends ObjectNode {
        @Property String s;
    }

    @Test
    public void testAdd() throws Exception {
        ListNode<TestElement> list = new ListNode<>(TestElement.class);

        TestElement element0 = new TestElement();
        list.add(element0);
        TestElement element1 = new TestElement();
        list.add(new TestElement());
        list.add(1, element1);

        assertEquals(Sets.newHashSet("0"), element0.getParentPaths().get(list));
        assertEquals(Sets.newHashSet("1"), element1.getParentPaths().get(list));
        assertEquals(Sets.newHashSet("2"), list.get(2).getParentPaths().get(list));

        TestElement elementMultiple = new TestElement();

        list.add(elementMultiple);
        list.add(2, elementMultiple);

        assertEquals(Sets.newHashSet("2", "4"), elementMultiple.getParentPaths().get(list));
    }

    @Test
    public void testAddAll() throws Exception {
        ListNode<TestElement> list = new ListNode<>(TestElement.class);

        List<TestElement> contents = Arrays.asList(new TestElement(), new TestElement(), new TestElement());
        contents.get(0).set("s", "content 0");
        contents.get(1).set("s", "content 1");
        contents.get(2).set("s", "content 2");

        list.add(new TestElement());
        list.add(new TestElement());

        list.addAll(1, contents);

        assertEquals("content 0", list.get(1).s);
        assertEquals("content 1", list.get(2).s);
        assertEquals("content 2", list.get(3).s);
        assertEquals(Sets.newHashSet("2"), list.get(2).getParentPaths().get(list));
        assertEquals(Sets.newHashSet("4"), list.get(4).getParentPaths().get(list));
    }

    @Test
    public void testClear() throws Exception {
        ListNode<TestElement> list = new ListNode<>(TestElement.class);

        list.add(new TestElement());
        list.add(new TestElement());
        list.add(new TestElement());
        list.add(new TestElement());

        TestElement element = list.get(2);

        list.clear();

        assertNull(element.getParentPaths().get(list));
    }

    @Test
    public void testListIterator() throws Exception {

    }

    @Test
    public void testRemove() throws Exception {

    }

    @Test
    public void testRemoveAll() throws Exception {

    }

    @Test
    public void testRetainAll() throws Exception {

    }

    @Test
    public void testSet() throws Exception {

    }

    @Test
    public void testSetAll() throws Exception {

    }

    @Test
    public void testGetElementType() throws Exception {

    }

    @Test
    public void testGetPropertyType() throws Exception {

    }

    @Test
    public void testGetAll() throws Exception {

    }

    @Test
    public void testTriggers() throws Exception {

    }

}
