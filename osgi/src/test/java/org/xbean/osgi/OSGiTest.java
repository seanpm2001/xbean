/**
 *
 * Copyright 2005 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.xbean.osgi;

import junit.framework.TestCase;
import org.eclipse.core.runtime.adaptor.EclipseAdaptor;
import org.eclipse.core.runtime.adaptor.LocationManager;
import org.eclipse.osgi.framework.adaptor.FrameworkAdaptor;
import org.eclipse.osgi.framework.internal.core.OSGi;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;

import java.io.File;
import java.util.Collections;

/**
 * @author Dain Sundstrom
 * @version $Id$
 * @since 2.0
 */
public class OSGiTest extends TestCase {
    private static final String basedir = System.getProperties().getProperty("basedir", ".");
    private OSGi osgi;
    private BundleContext bundleContext;
    private MavenBundleManager mavenBundleManager;

    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("osgi.clean", "true");

        File osgiDir = new File(basedir, "target/osgi");
        osgiDir.mkdirs();
        System.setProperty("osgi.configuration.area", osgiDir.getAbsolutePath());
        System.setProperty("osgi.install.area", osgiDir.getAbsolutePath());
        LocationManager.initializeLocations();

        FrameworkAdaptor adaptor = new EclipseAdaptor(null);
        osgi = new OSGi(adaptor);
        osgi.launch();
        bundleContext = osgi.getBundleContext();
        mavenBundleManager = new MavenBundleManager(bundleContext, new File("/Users/dain/.m2/repository/"));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        osgi.shutdown();
    }

    public void testAddBundel() throws Exception {
        Project project;
        Bundle bundle;
        BundleClassLoader bundleClassLoader;
        Class clazz = null;

        project = mavenBundleManager.loadProject("org.apache.geronimo.specs", "geronimo-spec-servlet", "2.4-rc4");
        bundle = mavenBundleManager.installBundle(project);
        bundleClassLoader = new BundleClassLoader(bundle);

        clazz = bundle.loadClass("javax.servlet.http.HttpServlet");
        assertNotNull(clazz);

        clazz = bundleClassLoader.loadClass("javax.servlet.http.HttpServlet");
        assertNotNull(clazz);

        assertSame(bundle, mavenBundleManager.installBundle(project));

        project = new Project("org.apache.geronimo.specs",
                "geronimo-spec-jsp",
                "2.0-rc4",
                "jar",
                Collections.singleton(new Dependency("org.apache.geronimo.specs", "geronimo-spec-servlet", "2.4-rc4", "jar")));
        bundle = mavenBundleManager.installBundle(project);
        bundleClassLoader = new BundleClassLoader(bundle);

        clazz = bundle.loadClass("javax.servlet.http.HttpServlet");
        assertNotNull(clazz);

        clazz = bundleClassLoader.loadClass("javax.servlet.http.HttpServlet");
        assertNotNull(clazz);

        clazz = bundle.loadClass("javax.servlet.jsp.HttpJspPage");
        assertNotNull(clazz);

        clazz = bundleClassLoader.loadClass("javax.servlet.jsp.HttpJspPage");
        assertNotNull(clazz);

        assertSame(bundle, mavenBundleManager.installBundle(project));
    }
}
