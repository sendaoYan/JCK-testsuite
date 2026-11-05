/*
 * @test
 * @summary org.w3c.dom.html.HTMLOptionElement conformance: reflection shape checks.
 * @run main HTMLOptionElementTest
 */

import java.lang.reflect.*;

public class HTMLOptionElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLOptionElement");
        check(c != null, "org.w3c.dom.html.HTMLOptionElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLOptionElement"), "binary name is org.w3c.dom.html.HTMLOptionElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLOptionElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLOptionElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getForm"), "declares getForm");
        check(declares(c, "getDefaultSelected"), "declares getDefaultSelected");
        check(declares(c, "setDefaultSelected"), "declares setDefaultSelected");
        check(declares(c, "getText"), "declares getText");
        check(declares(c, "getIndex"), "declares getIndex");
        check(declares(c, "getDisabled"), "declares getDisabled");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLOptionElement.class), "HTMLOptionElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLOptionElement.class.isInterface(), "HTMLOptionElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLOptionElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 13, "HTMLOptionElement declares exactly 13 methods");
        check(names.contains("getDefaultSelected"), "getDefaultSelected declared");
        check(names.contains("getDisabled"), "getDisabled declared");
        check(names.contains("getForm"), "getForm declared");
        check(names.contains("getIndex"), "getIndex declared");
        check(names.contains("getLabel"), "getLabel declared");
        check(names.contains("getSelected"), "getSelected declared");
        check(names.contains("getText"), "getText declared");
        check(names.contains("getValue"), "getValue declared");
        check(names.contains("setDefaultSelected"), "setDefaultSelected declared");
        check(names.contains("setDisabled"), "setDisabled declared");
        check(names.contains("setLabel"), "setLabel declared");
        check(names.contains("setSelected"), "setSelected declared");
        check(names.contains("setValue"), "setValue declared");
    }
}
