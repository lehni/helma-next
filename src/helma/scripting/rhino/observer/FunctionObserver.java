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

import helma.scripting.rhino.HopObject;

import java.util.ArrayList;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

class FunctionObserver extends BaseFunction implements Observer {
	private Function function;
	private Scriptable that;
	private Callable onChange;

	FunctionObserver(Function function, Scriptable that, Callable onChange) {
		this.function = function;
		this.that = that;
		this.onChange = onChange;
		setParentScope(function);
	}

	public Scriptable getObserved() {
        return function;
    }

	public Object call(Context cx, Scriptable scope, Scriptable thisObj,
			Object[] args) {
		// Observe results from functions too, as they might cause changes as
		// well...
	    
        // Detect changes on HopObjects by watching timestamps and call
        // onChange if the timestamp has changed.
	    // Also use the original observerd object if available, as for 
	    // example HopObjects are just wrapped in a normal ScriptableObject
	    // and could not be passed to native functions like this.
        ArrayList<HopObject> objects = null;
        ArrayList<Long> timestamps = null;
        if (onChange != null) {
            objects = new ArrayList<HopObject>();
            timestamps =  new ArrayList<Long>();
            Object object = that;
            if (object instanceof Observer)
                object = ((Observer) that).getObserved();
            if (object instanceof HopObject) {
                objects.add((HopObject) object);
                timestamps.add(((HopObject) object).getNode().lastModified());
            }
        }
        // Also convert passed arguments back to their original objects
        // and watch them for changes if they are HopObjects.
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Observer) {
                arg = ((Observer) arg).getObserved();
                args[i] = arg;
            }
            if (onChange != null && arg instanceof HopObject) {
                objects.add((HopObject) arg);
                timestamps.add(((HopObject) arg).getNode().lastModified());
            }
        }

	    // Call on the original object (= that), not on this.
        Object result = ObjectObserver.create(
                function.call(cx, scope, that, args), that, onChange);
       
        // Now if there were observed nodes, see if the function call
        // has modified their timestamps, and if so, fire onChange
        if (onChange != null) {
            for (int i = 0, l = objects.size(); i < l; i++) {
                HopObject object = objects.get(i);
                if (object.getNode().lastModified() != timestamps.get(i))
                    ObjectObserver.onChange(onChange, object);
            }
        }
		return result;
	}

	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		// Observe results from functions too, as they might cause changes as
		// well...
		return (Scriptable) ObjectObserver.create(function.construct(cx, scope,
				args), that, onChange);
	}

	public Object getDefaultValue(Class<?> typeHint) {
		return function.getDefaultValue(typeHint);
	}
}