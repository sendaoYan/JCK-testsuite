/*
 * @test
 * @summary org.w3c.dom.ls.LSProgressEvent conformance: reflection shape checks.
 * @run main LSProgressEventTest
 */

import java.lang.reflect.*;

public class LSProgressEventTest {
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
        Class<?> c = Class.forName("org.w3c.dom.ls.LSProgressEvent");
        check(c != null, "org.w3c.dom.ls.LSProgressEvent loads");
        check(c.getName().equals("org.w3c.dom.ls.LSProgressEvent"), "binary name is org.w3c.dom.ls.LSProgressEvent");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSProgressEvent is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSProgressEvent is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.events.Event")), "direct superinterfaces");
        check(declares(c, "getInput"), "declares getInput");
        check(declares(c, "getPosition"), "declares getPosition");
        check(declares(c, "getTotalSize"), "declares getTotalSize");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: LSProgressEvent contract (shape + producer absence) ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.events.Event.class.isAssignableFrom(org.w3c.dom.ls.LSProgressEvent.class), "LSProgressEvent extends Event");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.ls.LSProgressEvent.class.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getPosition") && names.contains("getTotalSize") && names.contains("getInput"), "getPosition/getTotalSize/getInput declared");
        check(names.size() == 3, "LSProgressEvent declares exactly three methods");
        check(org.w3c.dom.ls.LSProgressEvent.class.getDeclaredFields().length == 0, "LSProgressEvent declares no constants");
        org.w3c.dom.ls.LSProgressEvent ev = (org.w3c.dom.ls.LSProgressEvent) java.lang.reflect.Proxy.newProxyInstance(
            org.w3c.dom.ls.LSProgressEvent.class.getClassLoader(),
            new Class<?>[]{org.w3c.dom.ls.LSProgressEvent.class},
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    if (m.getName().equals("getPosition")) return 7;
                    if (m.getName().equals("getTotalSize")) return 100;
                    return null;
                }
            });
        check(ev.getPosition() == 7 && ev.getTotalSize() == 100, "proxy LSProgressEvent routes the progress counters");
        check(ev.getInput() == null, "proxy LSProgressEvent default null for un-routed getters");
        // synchronous parsing never produces progress events
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument().getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        org.w3c.dom.ls.LSInput in = ls.createLSInput(); in.setStringData("<r/>");
        check(p.parse(in) != null, "sync parse completes (progress notifications are async-only)");
    }
}
