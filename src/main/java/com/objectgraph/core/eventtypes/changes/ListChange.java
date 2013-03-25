package com.objectgraph.core.eventtypes.changes;

import java.util.Collections;
import java.util.List;

import com.objectgraph.core.ListNode;

public class ListChange extends Change {
	
	public enum ListChangeType {
		ADD, REMOVE
	}
	
	private final ListChangeType changeType;
	private final ListNode<?> list;
	private final List<?> elements;
	private final List<Integer> indices;
	private final boolean indirect;
	
	public <E> ListChange(ListChangeType changeType, ListNode<E> list, E element, int index) {
		this(changeType, list, element, index, false);
	}
	
	public <E> ListChange(ListChangeType changeType, ListNode<E> list, E element, int index, boolean indirect) {
		this.changeType = changeType;
		this.list = list;
		this.elements = Collections.unmodifiableList(Collections.singletonList(element));
		this.indices = Collections.unmodifiableList(Collections.singletonList(index));
		this.indirect = indirect;
	}
	
	public <E> ListChange(ListChangeType changeType, ListNode<E> list, List<E> elements, List<Integer> indices) {
		this.changeType = changeType;
		this.list = list;
		this.elements = Collections.unmodifiableList(elements);
		this.indices = Collections.unmodifiableList(indices);
		this.indirect = false;
	}
	
	public ListChangeType getChangeType() {
		return changeType;
	}

	public ListNode<?> getList() {
		return list;
	}

	public List<?> getElements() {
		return elements;
	}

	public List<Integer> getIndices() {
		return indices;
	}
	
	public boolean isIndirect() {
		return indirect;
	}

}
