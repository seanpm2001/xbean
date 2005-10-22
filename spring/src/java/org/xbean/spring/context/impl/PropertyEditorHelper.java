/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.xbean.spring.context.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyEditorManager;

/**
 * A helper method to register some custom editors
 * 
 * @version $Revision: 1.1 $
 */
public class PropertyEditorHelper {
    private static final Log log = LogFactory.getLog(PropertyEditorHelper.class);

    public static void registerCustomEditors() {
        registerEditor("java.net.URI", "org.xbean.spring.context.impl.URIEditor");
        registerEditor("javax.management.ObjectName", "org.xbean.spring.context.impl.ObjectNameEditor");
    }

    protected static void registerEditor(String typeName, String editorName) {
        Class type = loadClass(typeName);
        Class editor = loadClass(editorName);
        if (type != null && editor != null) {
            PropertyEditorManager.registerEditor(type, editor);
        }
    }

    public static Class loadClass(String name) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            try {
                return contextClassLoader.loadClass(name);
            }
            catch (ClassNotFoundException e) {
            }
        }
        try {
            return PropertyEditorHelper.class.getClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException e) {
            log.debug("Could not find class: " + name + " on the classpath");
            return null;
        }
    }

}
