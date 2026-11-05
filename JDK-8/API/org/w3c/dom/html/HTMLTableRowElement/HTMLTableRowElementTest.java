/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableRowElement conformance: reflection shape checks.
 * @run main HTMLTableRowElementTest
 */

import java.lang.reflect.*;

public class HTMLTableRowElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLTableRowElement");
        check(c != null, "org.w3c.dom.html.HTMLTableRowElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLTableRowElement"), "binary name is org.w3c.dom.html.HTMLTableRowElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLTableRowElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLTableRowElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getRowIndex"), "declares getRowIndex");
        check(declares(c, "getSectionRowIndex"), "declares getSectionRowIndex");
        check(declares(c, "getCells"), "declares getCells");
        check(declares(c, "getAlign"), "declares getAlign");
        check(declares(c, "setAlign"), "declares setAlign");
        check(declares(c, "getBgColor"), "declares getBgColor");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLTableRowElement.class), "HTMLTableRowElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLTableRowElement.class.isInterface(), "HTMLTableRowElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLTableRowElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 15, "HTMLTableRowElement declares exactly 15 methods");
        check(names.contains("deleteCell"), "deleteCell declared");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getBgColor"), "getBgColor declared");
        check(names.contains("getCells"), "getCells declared");
        check(names.contains("getCh"), "getCh declared");
        check(names.contains("getChOff"), "getChOff declared");
        check(names.contains("getRowIndex"), "getRowIndex declared");
        check(names.contains("getSectionRowIndex"), "getSectionRowIndex declared");
        check(names.contains("getVAlign"), "getVAlign declared");
        check(names.contains("insertCell"), "insertCell declared");
        check(names.contains("setAlign"), "setAlign declared");
        check(names.contains("setBgColor"), "setBgColor declared");
        check(names.contains("setCh"), "setCh declared");
        check(names.contains("setChOff"), "setChOff declared");
        check(names.contains("setVAlign"), "setVAlign declared");
    }
}
