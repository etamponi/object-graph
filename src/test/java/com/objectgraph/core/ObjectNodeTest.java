package com.objectgraph.core;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.objectgraph.core.Error.ErrorLevel;
import com.objectgraph.pluginsystem.PluginConfiguration;
import com.objectgraph.pluginsystem.PluginManager;
import com.objectgraph.utils.StringList;

public class ObjectNodeTest {
	
	public static class NodeExampleBase extends ObjectNode {
		@Property
		protected int property1;
		@Property
		protected String property2;
		@Property
		protected NodeExampleChild property3;
		
		protected NodeExampleChild falseProperty;
		
		public NodeExampleBase() {
			addTrigger(new Trigger<NodeExampleBase>() {

				@Override
				public List<String> getControlledPaths() {
					return new StringList("property1", "property3.property2");
				}

				@Override
				protected boolean isTriggeredBy(Event event) {
					return event.getPath().equals("property2") 
							&& getNode().get("property2").equals("enable");
				}

				@Override
				protected void action(Event event) {
					getNode().set("property1", 15);
					getNode().set("property3.property2", 3.1415);
					
					getNode().set("property2", "enabled");
				}
				
			});
			
			addErrorCheck(new ErrorCheck<NodeExampleBase>() {
				@Override
				public Error getError() {
					if ("enabled".equals(getNode().property2))
						return null;
					else
						return new Error(ErrorLevel.WARNING, "property2: still not set to \"enabled\"");
				}
			});
		}
	}
	
	public static class NodeExampleChild extends ObjectNode {
		@Property
		protected String property1;
		@Property
		protected double property2;
	}
	
	public static class NodeChildSubclass extends NodeExampleChild {
		@Property
		protected String property3;
	}

	@Test
	public void test() {
		NodeExampleBase base = new NodeExampleBase();
		assertEquals(new StringList("property1", "property2", "property3"), base.getProperties());
		assertEquals(new StringList("property2", "property3"), base.getFreeProperties());
		assertEquals(new StringList("property1"), base.getControlledProperties());
		
		NodeExampleChild child = new NodeExampleChild();
		assertTrue(child.getControlledProperties().isEmpty());
		base.set("property3", child);
		assertEquals(new StringList("property2"), child.getControlledProperties());
		assertEquals(0.0, child.get("property2"));
		
		Map<String, Error> errors = base.getErrors();
		assertEquals(1, errors.size());
		assertEquals("property2: still not set to \"enabled\"", errors.values().iterator().next().getMessage());
		assertEquals("property2: still not set to \"enabled\" (warning)", errors.values().iterator().next().toString());
		
		base.set("property2", "enable");
		assertEquals(3.1415, child.get("property2"));
		assertEquals(15, base.get("property1"));
		assertEquals("enabled", base.get("property2"));
		
		errors = base.getErrors();
		assertTrue(errors.isEmpty());
		
		assertSame(base.get("property3.property1"), child.get("property1"));
		
		base.set("property3", null);
		assertTrue(child.getControlledProperties().isEmpty());
		
		child.set("property2", 0.0);
		base.set("property2", "enable");
		
		assertEquals(0.0, child.get("property2"));
		
		assertEquals(NodeExampleChild.class, base.getPropertyType("property3", false));
		assertEquals(null, base.getPropertyType("property3", true));
		
		base.set("property3", new NodeChildSubclass());
		
		assertEquals(NodeExampleChild.class, base.getPropertyType("property3", false));
		assertEquals(NodeChildSubclass.class, base.getPropertyType("property3", true));
		
		PluginManager.setConfiguration(new PluginConfiguration("com.objectgraph"));
		assertEquals(2, base.getPossiblePropertyValues("property3").size());
		
		Node copy = base.property3.copy();
		assertEquals(3, copy.getFreeProperties().size());
		assertEquals(2, base.property3.getFreeProperties().size());
		copy = base.copy();
		assertEquals(2, copy.get("property3", Node.class).getFreeProperties().size());
	}

}
