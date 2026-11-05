/*
 * @test
 * @summary org.w3c.dom.css.Counter conformance: reflection shape checks.
 * @run main CounterTest
 */

import java.lang.reflect.*;

public class CounterTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.Counter");
        check(c != null, "org.w3c.dom.css.Counter loads");
        check(c.getName().equals("org.w3c.dom.css.Counter"), "binary name is org.w3c.dom.css.Counter");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.Counter is public");
        check(c.isInterface(), "org.w3c.dom.css.Counter is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getIdentifier"), "declares getIdentifier");
        check(declares(c, "getListStyle"), "declares getListStyle");
        check(declares(c, "getSeparator"), "declares getSeparator");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: Counter contract (shape + anonymous routing) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.css.Counter.class.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getIdentifier") && ms.contains("getListStyle") && ms.contains("getSeparator") && ms.size() == 3, "three accessor methods, no constants");
        org.w3c.dom.css.Counter c = new org.w3c.dom.css.Counter() {
            public String getIdentifier() { return "c"; }
            public String getListStyle() { return "decimal"; }
            public String getSeparator() { return "."; }
        };
        check("c".equals(c.getIdentifier()) && "decimal".equals(c.getListStyle()) && ".".equals(c.getSeparator()), "anonymous Counter returns the wired strings");
    }
}
