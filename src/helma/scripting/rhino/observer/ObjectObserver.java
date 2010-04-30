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
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.xml.XMLObject;

/**
 * @author lehni
 *
 */
public class ObjectObserver {
	public static Object create(Object object, Scriptable that, Callable onChange) {
		// Filter out easily detectable objects that need no observing
		if (object == null || object == Undefined.instance
				|| object == Scriptable.NOT_FOUND
				// filter out the ones that are already observed:
				|| object instanceof Observer
				// Do not observe native objects:
				|| object instanceof NativeJavaObject) {
			return object;
		}

        if (object instanceof Scriptable) {
            // Check if the object defines _ignoreChanges = true and therefore
            // does not need to be wrapped and watched for changes.
            Object ignoreChanges = ScriptableObject.getProperty(
                    (Scriptable) object, "__ignoreChanges__");
            if (ignoreChanges != Scriptable.NOT_FOUND
                    && ScriptRuntime.toBoolean(ignoreChanges)) {
                return object;
            }
            // As XMLList also seems to implement Function, check that one first
            if (object instanceof XMLObject) {
                return new XMLObjectObserver((XMLObject) object, onChange);
            } else if (object instanceof Function) {
                return new FunctionObserver((Function) object, that, onChange);
            } else if (object instanceof NativeArray) {
                return new ArrayObserver((NativeArray) object, onChange);
            } else {
                return new ScriptableObserver((Scriptable) object, onChange);
            }
        }
        // Basic types, no observing needed to detect change
        return object;
    }

    protected static void onChange(Callable onChange, Scriptable that, Object... args) {
        if (onChange != null) {
            Context cx = Context.getCurrentContext();
            Scriptable scope = ScriptableObject.getTopLevelScope(that);
            onChange.call(cx, scope, that, args);
        }
    }
}
