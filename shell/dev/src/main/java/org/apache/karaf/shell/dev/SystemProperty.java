/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.shell.dev;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.utils.properties.Properties;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.Constants;

/**
 * Command that allow access to system properties easily.
 */
@Command(scope = "dev", name = "system-property", description = "Get or set a system property.")
public class SystemProperty extends OsgiCommandSupport {

    @Option(name = "-p", aliases = {"--persistent"}, description = "Persist the new value to the etc/system.properties file")
    boolean persistent;

    @Option(name = "-f", aliases = {"--file-dump"}, description = "Dump system properties into a file (in the data folder)")
    boolean dumpToFile;

    @Option(name = "-u", aliases = {"--show-unset"}, description = "Show the OSGi properties even if unset")
    boolean unset;

    @Argument(index = 0, name = "key", required = false, description = "The system property name")
    String key;

    @Argument(index = 1, name = "value", required = false, description = "New value for the system property")
    String value;

    @Override
    protected Object doExecute() throws Exception {
        if (key == null && value == null) {
            java.util.Properties props = (java.util.Properties) System.getProperties().clone();

            String def = null;
            if (unset) {
                def = "unset";
            }

            setProperty(props, Constants.FRAMEWORK_BEGINNING_STARTLEVEL, def);
            setProperty(props, Constants.FRAMEWORK_BOOTDELEGATION, def);
            setProperty(props, Constants.FRAMEWORK_BUNDLE_PARENT, def);
            setProperty(props, Constants.FRAMEWORK_BUNDLE_PARENT_APP, def);
            setProperty(props, Constants.FRAMEWORK_BUNDLE_PARENT_BOOT, def);
            setProperty(props, Constants.FRAMEWORK_BUNDLE_PARENT_EXT, def);
            setProperty(props, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK, def);
            setProperty(props, Constants.FRAMEWORK_EXECPERMISSION, def);
            setProperty(props, Constants.FRAMEWORK_EXECUTIONENVIRONMENT, def);
            setProperty(props, Constants.FRAMEWORK_LANGUAGE, def);
            setProperty(props, Constants.FRAMEWORK_LIBRARY_EXTENSIONS, def);
            setProperty(props, Constants.FRAMEWORK_OS_NAME, def);
            setProperty(props, Constants.FRAMEWORK_OS_VERSION, def);
            setProperty(props, Constants.FRAMEWORK_PROCESSOR, def);
            setProperty(props, Constants.FRAMEWORK_SECURITY, def);
            setProperty(props, Constants.FRAMEWORK_SECURITY_OSGI, def);
            setProperty(props, Constants.FRAMEWORK_STORAGE, def);
            setProperty(props, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT, def);
            setProperty(props, Constants.FRAMEWORK_SYSTEMPACKAGES, def);
            setProperty(props, Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, def);
            setProperty(props, Constants.FRAMEWORK_VENDOR, def);
            setProperty(props, Constants.FRAMEWORK_VERSION, def);
            setProperty(props, Constants.FRAMEWORK_WINDOWSYSTEM, def);

            setProperty(props, Constants.SUPPORTS_BOOTCLASSPATH_EXTENSION, def);
            setProperty(props, Constants.SUPPORTS_FRAMEWORK_EXTENSION, def);
            setProperty(props, Constants.SUPPORTS_FRAMEWORK_FRAGMENT, def);
            setProperty(props, Constants.SUPPORTS_FRAMEWORK_REQUIREBUNDLE, def);

            if (dumpToFile) {
                PrintStream ps = new PrintStream(new File(bundleContext.getProperty("karaf.data"), "dump-properties-" + System.currentTimeMillis() + ".properties"));
                ps.println("#Dump of the System and OSGi properties with the command dev:system-property");
                ps.println("#Dump executed at " + new SimpleDateFormat().format(new Date()));
                printOrderedProperties(props, ps);
                ps.flush();
                ps.close();
            } else {
                System.out.println("OSGi and System properties: ");
                printOrderedProperties(props, System.out);
            }

            return null;
        }

        if (value != null) {
            if (persistent) {
                String base = System.getProperty("karaf.base");
                Properties props = new Properties(new File(base, "etc/system.properties"));
                props.put(key, value);
                props.save();
            }
            System.setProperty(key, value);
        } else {
            System.out.println(System.getProperty(key));
        }

        return null;
    }

    private void setProperty(java.util.Properties props, String key, String def) {
        String val = bundleContext.getProperty(key);
        if (val == null && def != null) {
            props.setProperty(key, def);
        } else if (val != null) {
            props.setProperty(key, val);
        }
    }

    private void printOrderedProperties(java.util.Properties props, PrintStream out) {
        Set<Object> keys = props.keySet();
        Vector<String> order = new Vector<String>(keys.size());
        for (Iterator<Object> i = keys.iterator(); i.hasNext(); ) {
            Object str = (Object) i.next();
            order.add((String) str);
        }
        Collections.sort(order);
        for (Iterator<String> i = order.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            out.println(key + "=" + props.getProperty(key));
        }
    }

}
