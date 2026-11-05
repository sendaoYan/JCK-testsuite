/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableSectionElement conformance: reflection shape checks.
 * @run main HTMLTableSectionElementTest
 */

import java.lang.reflect.*;

public class HTMLTableSectionElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLTableSectionElement");
        check(c != null, "org.w3c.dom.html.HTMLTableSectionElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLTableSectionElement"), "binary name is org.w3c.dom.html.HTMLTableSectionElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLTableSectionElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLTableSectionElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getAlign"), "declares getAlign");
        check(declares(c, "setAlign"), "declares setAlign");
        check(declares(c, "getCh"), "declares getCh");
        check(declares(c, "setCh"), "declares setCh");
        check(declares(c, "getChOff"), "declares getChOff");
        check(declares(c, "setChOff"), "declares setChOff");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLTableSectionElement.class), "HTMLTableSectionElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLTableSectionElement.class.isInterface(), "HTMLTableSectionElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLTableSectionElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 11, "HTMLTableSectionElement declares exactly 11 methods");
        check(names.contains("deleteRow"), "deleteRow declared");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getCh"), "getCh declared");
        check(names.contains("getChOff"), "getChOff declared");
        check(names.contains("getRows"), "getRows declared");
        check(names.contains("getVAlign"), "getVAlign declared");
        check(names.contains("insertRow"), "insertRow declared");
        check(names.contains("setAlign"), "setAlign declared");
        check(names.contains("setCh"), "setCh declared");
        check(names.contains("setChOff"), "setChOff declared");
        check(names.contains("setVAlign"), "setVAlign declared");
    }
}
