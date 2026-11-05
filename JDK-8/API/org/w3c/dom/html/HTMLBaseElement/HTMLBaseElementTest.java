/*
 * @test
 * @summary org.w3c.dom.html.HTMLBaseElement conformance: reflection shape checks.
 * @run main HTMLBaseElementTest
 */

import java.lang.reflect.*;

public class HTMLBaseElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLBaseElement");
        check(c != null, "org.w3c.dom.html.HTMLBaseElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLBaseElement"), "binary name is org.w3c.dom.html.HTMLBaseElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLBaseElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLBaseElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getHref"), "declares getHref");
        check(declares(c, "setHref"), "declares setHref");
        check(declares(c, "getTarget"), "declares getTarget");
        check(declares(c, "setTarget"), "declares setTarget");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLBaseElement.class), "HTMLBaseElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLBaseElement.class.isInterface(), "HTMLBaseElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLBaseElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 4, "HTMLBaseElement declares exactly 4 methods");
        check(names.contains("getHref"), "getHref declared");
        check(names.contains("getTarget"), "getTarget declared");
        check(names.contains("setHref"), "setHref declared");
        check(names.contains("setTarget"), "setTarget declared");
    }
}
