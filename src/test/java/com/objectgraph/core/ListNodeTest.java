package com.objectgraph.core;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.junit.Test;

import com.objectgraph.core.eventtypes.changes.Change;
import com.objectgraph.core.eventtypes.changes.ListChange;
import com.objectgraph.core.eventtypes.changes.ListChange.ListChangeType;
import com.objectgraph.utils.StringList;

public class ListNodeTest {
	
	public static class Example extends ObjectNode {
		@Property
		protected ListNode<String> list1 = new ListNode<>(String.class);
		@Property
		protected ListNode<Element> list2 = new ListNode<>(Element.class);
		@Property
		protected String property1;
		
		public Example() {
			initialiseNode();
			addTrigger(new Trigger<Example>() {

				@Override
				public List<String> getControlledPaths() {
					return new StringList("list2.*.property1");
				}

				@Override
				protected boolean isTriggeredBy(Event event) {
					if (event.getPath().equals("list1") && event.getType() instanceof ListChange) {
						ListChange type = (ListChange)event.getType();
						if (type.getChangeType().equals(ListChangeType.REMOVE)) {
							return type.getIndices().contains(2);
						}
					}
					return false;
				}

				@Override
				protected void action(Event event) {
					getNode().set("list2.*.property1", getNode().get("property1"));
				}
			});
		}
	}
	
	public static class Element extends ObjectNode {
		@Property
		protected String property1;
		@Property
		protected double property2;
	}

	@Test
	public void test() {
		ListNode<String> list = new ListNode<>(String.class);
		
		list.add("Ciao");
		list.add("Eccoci");
		
		assertEquals(new StringList("0", "1"), list.getProperties());
		
		list.remove("Ciao");
		
		assertEquals(new StringList("0"), list.getProperties());
		
		Example object = new Example();
		
		object.set("property1", "Automatic");
		object.set("list1", list);
		assertEquals("Eccoci", object.get("list1.0"));
		
		object.get("list1", ListNode.class).add("Prova");
		assertEquals("Prova", object.get("list1.1"));

		object.get("list2", ListNode.class).add(new Element());
		object.get("list2", ListNode.class).add(new Element());
		assertEquals(new StringList("property1"), object.get("list2.0", Element.class).getControlledProperties());
		assertEquals(null, object.get("list2.1.property1"));
		
		list.add("ciao");
		assertEquals(null, object.get("list2.1.property1"));
		list.remove("ciao");
		assertEquals("Automatic", object.get("list2.1.property1"));
		
		assertEquals(new StringList("Automatic", "Automatic"), object.get("list2.*.property1"));

		assertEquals(Element.class, object.get("list2", ListNode.class).getPropertyType("1", false));
		assertEquals(Element.class, object.get("list2", ListNode.class).getPropertyType("1", true));
	}
	
	@Test
	public void testIterator() {
		ListNode<String> list = new ListNode<>(String.class, "Test1", "Test2", "Test3");
		list.addTrigger(new Trigger<ListNode<String>>() {

			@Override
			public List<String> getControlledPaths() {
				return Collections.emptyList();
			}

			@Override
			protected boolean isTriggeredBy(Event event) {
				return event.getType() instanceof Change;
			}

			@Override
			protected void action(Event event) {
				if (event.getType() instanceof ListChange) {
					if (!event.getType(ListChange.class).isIndirect())
						getNode().add("TestAutomatic");
				}
			}
			
		});
		ListIterator<String> it = list.listIterator(1);
		assertEquals("Test2", it.next());
		it.remove();
		assertEquals(2, list.size());
		assertEquals("Test3", it.next());
		it.add("TestManual");
		assertEquals(3, list.size());
		assertEquals("TestManual", list.get(2));
	}

}
