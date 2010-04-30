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

import org.mozilla.javascript.Scriptable;

/**
 * @author lehni
 *
 */
interface Observer {
    public Scriptable getObserved();
}
