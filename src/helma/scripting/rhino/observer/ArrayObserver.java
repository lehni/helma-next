/*
 * Helma License Notice
 *
 * The contents of this file are subject to the Helma License
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://adele.helma.org/download/helma/license.txt
 *
 * Copyright 1998-2003 Helma Software. All Rights Reserved.
 *
 * $RCSfile$
 * $Author: lehni $
 * $Revision: 9626 $
 * $Date: 2009-04-17 16:49:26 +0200 (Fri, 17 Apr 2009) $
 */


package helma.scripting.rhino.observer;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

/**
 * @author lehni
 *
 */
public class ArrayObserver extends NativeArray implements Wrapper, Observer  {

	private NativeArray array;
	private Callable onChange;

	public ArrayObserver(NativeArray object, Callable onChange) {
		super(0);
		this.array = object;
		this.onChange = onChange;
	}

    public Scriptable getObserved() {
        return array;
    }

    public String getClassName() {
		return array.getClassName();
	}

	public Object[] getIds() {
		return array.getIds();
	}

	public Scriptable getPrototype() {
		return array.getPrototype();
	}

	public void setPrototype(Scriptable prototype) {
		array.setPrototype(prototype);
	}

	public Scriptable getParentScope() {
		return array.getParentScope();
	}

	public Object getDefaultValue(Class<?> hint) {
		return array.getDefaultValue(hint);
	}

	public boolean hasInstance(Scriptable instance) {
		return array.hasInstance(instance);
	}

	public String toString() {
		return array.toString();
	}

	public int hashCode() {
		return array.hashCode();
	}

	public Object unwrap() {
		return array;
	}

	public Object get(String name, Scriptable start) {
		// Observe sub elements again
		return ObjectObserver.create(
				ScriptableObject.getProperty(array, name), array, onChange);
	}

	public Object get(int index, Scriptable start) {
		// Observe sub elements again
		return ObjectObserver.create(
				ScriptableObject.getProperty(array, index), array, onChange);
	}

	public void put(String name, Scriptable start, Object value) {
		// Observe values that are put in too as they might be changed after
		ScriptableObject.putProperty(array, name,
				ObjectObserver.create(value, array, onChange));
		if (onChange != null)
            ObjectObserver.onChange(onChange, this, name, value);
	}

	public void put(int index, Scriptable start, Object value) {
		// Observe values that are put in too as they might be changed after
		ScriptableObject.putProperty(array, index,
				ObjectObserver.create(value, array, onChange));
        if (onChange != null)
            ObjectObserver.onChange(onChange, this, Integer.toString(index),
                    value);
	}

	public boolean has(String name, Scriptable start) {
		return ScriptableObject.hasProperty(array, name);
	}

	public boolean has(int index, Scriptable start) {
		return ScriptableObject.hasProperty(array, index);
	}

	public void delete(String name) {
		if (ScriptableObject.deleteProperty(array, name))
		    if (onChange != null)
                ObjectObserver.onChange(onChange, this, name);
	}

	public void delete(int index) {
		if (ScriptableObject.deleteProperty(array, index))
		    if (onChange != null)
                ObjectObserver.onChange(onChange, this, Integer.toString(index));
	}
}
