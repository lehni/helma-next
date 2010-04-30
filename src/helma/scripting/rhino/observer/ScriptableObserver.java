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
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

/**
 * @author lehni
 *
 */
public class ScriptableObserver extends ScriptableObject implements Wrapper, Observer {
	private Scriptable object;
	private Callable onChange;

	ScriptableObserver(Scriptable object, Callable onChange) {
		this.object = object;
		this.onChange = onChange;
	}

    public Scriptable getObserved() {
        return object;
    }

    public String getClassName() {
		return object.getClassName();
	}

	public Object[] getIds() {
		return object.getIds();
	}

	public Scriptable getPrototype() {
		return object.getPrototype();
	}

	public void setPrototype(Scriptable prototype) {
		object.setPrototype(prototype);
	}

	public Scriptable getParentScope() {
		return object.getParentScope();
	}

	public Object getDefaultValue(Class<?> hint) {
		return object.getDefaultValue(hint);
	}

	public boolean hasInstance(Scriptable instance) {
		return object.hasInstance(instance);
	}

	public String toString() {
		return object.toString();
	}

	public int hashCode() {
		return object.hashCode();
	}

	public Object unwrap() {
		return object instanceof Wrapper ? ((Wrapper) object).unwrap() : object;
	}

	public Object get(String name, Scriptable start) {
		// Observe sub elements again
		return ObjectObserver.create(
				ScriptableObject.getProperty(object, name), object, onChange);
	}

	public Object get(int index, Scriptable start) {
		// Observe sub elements again
		return ObjectObserver.create(
				ScriptableObject.getProperty(object, index), object, onChange);
	}

	public void put(String name, Scriptable start, Object value) {
		// Observe values that are put in too as they might be changed after
		ScriptableObject.putProperty(object, name,
				ObjectObserver.create(value, object, onChange));
		if (onChange != null)
            ObjectObserver.onChange(onChange, this, name, value);
	}

	public void put(int index, Scriptable start, Object value) {
		// Observe values that are put in too as they might be changed after
		ScriptableObject.putProperty(object, index,
				ObjectObserver.create(value, object, onChange));
        if (onChange != null)
            ObjectObserver.onChange(onChange, this, Integer.toString(index),
                    value);
	}

	public boolean has(String name, Scriptable start) {
		return ScriptableObject.hasProperty(object, name);
	}

	public boolean has(int index, Scriptable start) {
		return ScriptableObject.hasProperty(object, index);
	}

	public void delete(String name) {
		if (ScriptableObject.deleteProperty(object, name))
		    if (onChange != null)
	            ObjectObserver.onChange(onChange, this, name);
	}

	public void delete(int index) {
		if (ScriptableObject.deleteProperty(object, index))
            if (onChange != null)
                ObjectObserver.onChange(onChange, this, Integer.toString(index));
	}
}
