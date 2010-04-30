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
 * $Author$
 * $Revision$
 * $Date$
 */


package helma.scripting.rhino.observer;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeWith;
import org.mozilla.javascript.Ref;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.xml.XMLObject;

/**
 * @author lehni
 * 
 */
public class XMLObjectObserver extends XMLObject implements Wrapper, Observer {
	private XMLObject object;
	private Callable onChange;

	XMLObjectObserver(XMLObject object, Callable onChange) {
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
		return object;
	}

	public Object ecmaGet(Context cx, Object id) {
		// Observe sub elements again
		return ObjectObserver.create(object.ecmaGet(cx, id), object, onChange);
	}

	public void ecmaPut(Context cx, Object id, Object value) {
		// Observe values that are put in too as they might be changed after
		object.ecmaPut(cx, id,
				ObjectObserver.create(value, object, onChange));
		if (onChange != null)
		    ObjectObserver.onChange(onChange, this, id.toString(), value);
	}

	public boolean ecmaHas(Context cx, Object id) {
		return object.ecmaHas(cx, id);
	}

	public boolean ecmaDelete(Context cx, Object id) {
		boolean ret = object.ecmaDelete(cx, id);
        if (ret && onChange != null)
            ObjectObserver.onChange(onChange, this, id.toString());
		return ret;
	}

	public NativeWith enterDotQuery(Scriptable scope) {
		return object.enterDotQuery(scope);
	}

	public NativeWith enterWith(Scriptable scope) {
		return object.enterWith(scope);
	}

	public Scriptable getExtraMethodSource(Context cx) {
		return object.getExtraMethodSource(cx);
	}

	public Ref memberRef(Context cx, Object elem, int memberTypeFlags) {
		return object.memberRef(cx, elem, memberTypeFlags);
	}

	public Ref memberRef(Context cx, Object namespace, Object elem,
			int memberTypeFlags) {
		return object.memberRef(cx, namespace, elem, memberTypeFlags);
	}
}
