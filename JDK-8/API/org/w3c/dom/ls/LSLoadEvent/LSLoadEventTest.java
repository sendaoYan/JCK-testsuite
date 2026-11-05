/*
 * @test
 * @summary org.w3c.dom.ls.LSLoadEvent conformance: reflection shape checks.
 * @run main LSLoadEventTest
 */

import java.lang.reflect.*;

public class LSLoadEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.ls.LSLoadEvent");
        check(c != null, "org.w3c.dom.ls.LSLoadEvent loads");
        check(c.getName().equals("org.w3c.dom.ls.LSLoadEvent"), "binary name is org.w3c.dom.ls.LSLoadEvent");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSLoadEvent is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSLoadEvent is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.events.Event")), "direct superinterfaces");
        check(declares(c, "getNewDocument"), "declares getNewDocument");
        check(declares(c, "getInput"), "declares getInput");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: LSLoadEvent contract (shape + producer absence) ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.events.Event.class.isAssignableFrom(org.w3c.dom.ls.LSLoadEvent.class), "LSLoadEvent extends Event");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.ls.LSLoadEvent.class.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getNewDocument") && names.contains("getInput"), "getNewDocument/getInput declared");
        check(names.size() == 2, "LSLoadEvent declares exactly two methods");
        // no constants of its own
        check(org.w3c.dom.ls.LSLoadEvent.class.getDeclaredFields().length == 0, "LSLoadEvent declares no constants");
        // an anonymous implementation honors the getter contract
        final org.w3c.dom.Document marker = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final org.w3c.dom.ls.LSInput markerIn = ((org.w3c.dom.ls.DOMImplementationLS) marker.getImplementation().getFeature("LS", "3.0")).createLSInput();
        org.w3c.dom.ls.LSLoadEvent ev = (org.w3c.dom.ls.LSLoadEvent) java.lang.reflect.Proxy.newProxyInstance(
            org.w3c.dom.ls.LSLoadEvent.class.getClassLoader(),
            new Class<?>[]{org.w3c.dom.ls.LSLoadEvent.class},
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    if (m.getName().equals("getNewDocument")) return marker;
                    if (m.getName().equals("getInput")) return markerIn;
                    return null;
                }
            });
        check(ev.getNewDocument() == marker && ev.getInput() == markerIn, "proxy LSLoadEvent routes getNewDocument/getInput");
        // synchronous parsing never produces LSLoadEvent (async-only notification)
        final java.util.List<Object> events = new java.util.ArrayList<Object>();
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) marker.getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        org.w3c.dom.ls.LSInput in = ls.createLSInput();
        in.setStringData("<r/>");
        check(p.parse(in) != null && events.isEmpty(), "synchronous parse yields no LSLoadEvent (async-only event)");
    }
}
