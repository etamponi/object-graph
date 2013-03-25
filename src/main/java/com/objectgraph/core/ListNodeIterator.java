package com.objectgraph.core;

import java.util.ListIterator;

import com.objectgraph.core.eventtypes.changes.ListChange;
import com.objectgraph.core.eventtypes.changes.ListChange.ListChangeType;

public class ListNodeIterator<E> implements ListIterator<E> {
	
	private final ListIterator<E> iterator;
	
	private final ListNode<E> list;
	
	public ListNodeIterator(ListNode<E> list, ListIterator<E> iterator) {
		this.list = list;
		this.iterator = iterator;
	}

	@Override
	public void add(E element) {
		int index = iterator.nextIndex();
		iterator.add(element);
		if (element instanceof Node)
			((Node)element).addParentPath(list, String.valueOf(index));
		
		for(index = index+1; index < list.size(); index++) {
			E current = list.get(index);
			if (current instanceof Node) {
				String oldPath = String.valueOf(index - 1);
				String newPath = String.valueOf(index);
				((Node)current).removeParentPath(list, oldPath);
				((Node)current).addParentPath(list, newPath);
			}
		}
		
		list.updatePropertyList();
		
		list.fireEvent(new Event("", new ListChange(ListChangeType.ADD, list, element, iterator.nextIndex(), true)));
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public boolean hasPrevious() {
		return iterator.hasPrevious();
	}

	@Override
	public E next() {
		return iterator.next();
	}

	@Override
	public int nextIndex() {
		return iterator.nextIndex();
	}

	@Override
	public E previous() {
		return iterator.previous();
	}

	@Override
	public int previousIndex() {
		return iterator.previousIndex();
	}

	@Override
	public void remove() {
		int index = iterator.nextIndex()-1;
		E element = list.get(index);
		iterator.remove();
		if (element instanceof Node)
			((Node)element).addParentPath(list, String.valueOf(index));
		
		for(int i = index; i < list.size(); i++) {
			E current = list.get(i);
			if (current instanceof Node) {
				String oldPath = String.valueOf(i + 1);
				String newPath = String.valueOf(i);
				((Node)current).removeParentPath(list, oldPath);
				((Node)current).addParentPath(list, newPath);
			}
		}
		
		list.updatePropertyList();
		
		list.fireEvent(new Event("", new ListChange(ListChangeType.REMOVE, list, element, index, true)));
	}

	@Override
	public void set(E element) {
		list.set(iterator.nextIndex()-1, element);
	}

}
