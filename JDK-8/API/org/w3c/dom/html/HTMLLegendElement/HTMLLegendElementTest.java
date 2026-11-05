/*
 * @test
 * @summary org.w3c.dom.html.HTMLLegendElement conformance: reflection shape checks.
 * @run main HTMLLegendElementTest
 */

import java.lang.reflect.*;

public class HTMLLegendElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLLegendElement");
        check(c != null, "org.w3c.dom.html.HTMLLegendElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLLegendElement"), "binary name is org.w3c.dom.html.HTMLLegendElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLLegendElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLLegendElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getForm"), "declares getForm");
        check(declares(c, "getAccessKey"), "declares getAccessKey");
        check(declares(c, "setAccessKey"), "declares setAccessKey");
        check(declares(c, "getAlign"), "declares getAlign");
        check(declares(c, "setAlign"), "declares setAlign");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLLegendElement.class), "HTMLLegendElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLLegendElement.class.isInterface(), "HTMLLegendElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLLegendElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 5, "HTMLLegendElement declares exactly 5 methods");
        check(names.contains("getAccessKey"), "getAccessKey declared");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getForm"), "getForm declared");
        check(names.contains("setAccessKey"), "setAccessKey declared");
        check(names.contains("setAlign"), "setAlign declared");
    }
}
