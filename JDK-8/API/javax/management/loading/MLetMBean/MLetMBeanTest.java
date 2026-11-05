/*
 * @test
 * @summary javax.management.loading.MLetMBean conformance: interface reflection.
 * @run main MLetMBeanTest
 */

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ServiceNotFoundException;
import javax.management.loading.MLet;
import javax.management.loading.MLetMBean;

public class MLetMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.MLetMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addURL"), "declares addURL");
        check(hasMethod(c, "getLibraryDirectory"), "declares getLibraryDirectory");
        check(hasMethod(c, "getMBeansFromURL"), "declares getMBeansFromURL");
        // ---- deepened: exact interface surface (javap -public verified) ----
        testExactMethodSet();
        // ---- deepened: MLet is-a MLetMBean + every method callable ----
        testCallableThroughInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: exact interface surface (javap -public verified) ----
    static void testExactMethodSet() throws Exception {
        // exactly 10 abstract methods, all public+abstract, no fields, no static/default methods
        Method[] ms = cD().getMethods();
        Set<String> names = new HashSet<String>();
        for (Method m : ms) {
            names.add(m.getName() + ":" + m.getParameterTypes().length);
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method public abstract: " + m.getName());
            // every method declares either ServiceNotFoundException or IOException or none
            for (Class<?> ex : m.getExceptionTypes()) {
                check(ex == ServiceNotFoundException.class || ex == java.io.IOException.class,
                        "declared exception allowed: " + ex.getName());
            }
        }
        check(ms.length == 10, "exactly 10 interface methods, got " + ms.length);
        check(cD().getDeclaredFields().length == 0, "no fields");
        Set<String> uniq = new HashSet<String>(names);
        check(uniq.contains("getURLs:0") && uniq.contains("getResource:1")
                && uniq.contains("getResourceAsStream:1") && uniq.contains("getResources:1")
                && uniq.contains("getLibraryDirectory:0") && uniq.contains("setLibraryDirectory:1"),
                "URL/resource/library management methods present");
        check(uniq.contains("getMBeansFromURL:1") && uniq.contains("addURL:1"),
                "overloaded families present");
        // MLetMBean itself extends no other interface
        check(cD().getInterfaces().length == 0, "no extended interfaces");
        // interface is implemented by MLet (is-a)
        check(MLetMBean.class.isAssignableFrom(MLet.class), "MLet implements MLetMBean");
    }

    static Class<?> cD() throws Exception {
        return Class.forName("javax.management.loading.MLetMBean");
    }

    // ---- deepened: MLet is-a MLetMBean + every method callable ----
    static void testCallableThroughInterface() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        MLet impl = new MLet();
        check(impl instanceof MLetMBean, "MLet instanceof MLetMBean");
        // every method of the interface is invocable through the interface reference
        MLetMBean view = (MLetMBean) impl;
        URL u = new URL("file:/jck/mlet/dir/");
        view.addURL(u);
        check(view.getURLs().length == 1 && u.equals(view.getURLs()[0]),
                "addURL(URL)/getURLs through interface view");
        view.addURL("file:/jck/mlet/dir2/");
        check(view.getURLs().length == 2, "addURL(String) through interface view");
        check(view.getResource("java/lang/String.class") != null,
                "getResource through interface view");
        InputStream is = view.getResourceAsStream("java/lang/String.class");
        check(is != null, "getResourceAsStream through interface view");
        is.close();
        Enumeration<URL> en = view.getResources("java/lang/String.class");
        check(en != null && en.hasMoreElements(), "getResources through interface view");
        view.setLibraryDirectory("/jck/lib");
        check("/jck/lib".equals(view.getLibraryDirectory()),
                "set/getLibraryDirectory through interface view");
        // getMBeansFromURL(URL)/(String): both reachable through the view;
        // unregistered state gives IllegalStateException (String) / SNF for null (URL)
        try {
            view.getMBeansFromURL((URL) null);
            check(false, "getMBeansFromURL(URL null) expected SNF");
        } catch (ServiceNotFoundException e) {
            check("The specified URL is null".equals(e.getMessage()),
                    "view getMBeansFromURL(URL null) message");
        }
        try {
            view.getMBeansFromURL("file:/no/such.mlet");
            check(false, "getMBeansFromURL(String) unregistered expected ISE");
        } catch (IllegalStateException e) {
            passed++;
        }
        // registered: (String) reaches the parser and fails with URL-naming SNF
        ObjectName on = new ObjectName("jck.mletmbean:o=view");
        server.registerMBean(impl, on);
        try {
            view.getMBeansFromURL(new URL("file:/no/such2.mlet"));
            check(false, "getMBeansFromURL(URL) missing file expected SNF");
        } catch (ServiceNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("file:/no/such2.mlet"),
                    "registered view getMBeansFromURL(URL) names the URL");
        }
        server.unregisterMBean(on);
    }
}

