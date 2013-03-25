package com.objectgraph.core;

import java.util.List;
import java.util.ListIterator;

public abstract class Constraint<N extends Node, T> extends NodeHelper<N> {
	
	protected abstract boolean check(T element);
	
	public final void apply(List<T> list) {
		ListIterator<T> it = list.listIterator();
		while(it.hasNext()) {
			T t = it.next();
			if (!check(t))
				it.remove();
		}
	}

}
