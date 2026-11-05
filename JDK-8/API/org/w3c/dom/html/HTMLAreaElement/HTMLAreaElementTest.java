/*
 * @test
 * @summary org.w3c.dom.html.HTMLAreaElement conformance: reflection shape checks.
 * @run main HTMLAreaElementTest
 */

import java.lang.reflect.*;

public class HTMLAreaElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLAreaElement");
        check(c != null, "org.w3c.dom.html.HTMLAreaElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLAreaElement"), "binary name is org.w3c.dom.html.HTMLAreaElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLAreaElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLAreaElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getAccessKey"), "declares getAccessKey");
        check(declares(c, "setAccessKey"), "declares setAccessKey");
        check(declares(c, "getAlt"), "declares getAlt");
        check(declares(c, "setAlt"), "declares setAlt");
        check(declares(c, "getCoords"), "declares getCoords");
        check(declares(c, "setCoords"), "declares setCoords");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLAreaElement.class), "HTMLAreaElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLAreaElement.class.isInterface(), "HTMLAreaElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLAreaElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 16, "HTMLAreaElement declares exactly 16 methods");
        check(names.contains("getAccessKey"), "getAccessKey declared");
        check(names.contains("getAlt"), "getAlt declared");
        check(names.contains("getCoords"), "getCoords declared");
        check(names.contains("getHref"), "getHref declared");
        check(names.contains("getNoHref"), "getNoHref declared");
        check(names.contains("getShape"), "getShape declared");
        check(names.contains("getTabIndex"), "getTabIndex declared");
        check(names.contains("getTarget"), "getTarget declared");
        check(names.contains("setAccessKey"), "setAccessKey declared");
        check(names.contains("setAlt"), "setAlt declared");
        check(names.contains("setCoords"), "setCoords declared");
        check(names.contains("setHref"), "setHref declared");
        check(names.contains("setNoHref"), "setNoHref declared");
        check(names.contains("setShape"), "setShape declared");
        check(names.contains("setTabIndex"), "setTabIndex declared");
        check(names.contains("setTarget"), "setTarget declared");
    }
}
