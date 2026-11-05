/*
 * @test
 * @summary org.w3c.dom.html.HTMLBodyElement conformance: reflection shape checks.
 * @run main HTMLBodyElementTest
 */

import java.lang.reflect.*;

public class HTMLBodyElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLBodyElement");
        check(c != null, "org.w3c.dom.html.HTMLBodyElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLBodyElement"), "binary name is org.w3c.dom.html.HTMLBodyElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLBodyElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLBodyElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getALink"), "declares getALink");
        check(declares(c, "setALink"), "declares setALink");
        check(declares(c, "getBackground"), "declares getBackground");
        check(declares(c, "setBackground"), "declares setBackground");
        check(declares(c, "getBgColor"), "declares getBgColor");
        check(declares(c, "setBgColor"), "declares setBgColor");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLBodyElement.class), "HTMLBodyElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLBodyElement.class.isInterface(), "HTMLBodyElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLBodyElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 12, "HTMLBodyElement declares exactly 12 methods");
        check(names.contains("getALink"), "getALink declared");
        check(names.contains("getBackground"), "getBackground declared");
        check(names.contains("getBgColor"), "getBgColor declared");
        check(names.contains("getLink"), "getLink declared");
        check(names.contains("getText"), "getText declared");
        check(names.contains("getVLink"), "getVLink declared");
        check(names.contains("setALink"), "setALink declared");
        check(names.contains("setBackground"), "setBackground declared");
        check(names.contains("setBgColor"), "setBgColor declared");
        check(names.contains("setLink"), "setLink declared");
        check(names.contains("setText"), "setText declared");
        check(names.contains("setVLink"), "setVLink declared");
    }
}
