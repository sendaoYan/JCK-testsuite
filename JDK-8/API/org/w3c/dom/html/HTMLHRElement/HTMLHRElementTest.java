/*
 * @test
 * @summary org.w3c.dom.html.HTMLHRElement conformance: reflection shape checks.
 * @run main HTMLHRElementTest
 */

import java.lang.reflect.*;

public class HTMLHRElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLHRElement");
        check(c != null, "org.w3c.dom.html.HTMLHRElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLHRElement"), "binary name is org.w3c.dom.html.HTMLHRElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLHRElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLHRElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getAlign"), "declares getAlign");
        check(declares(c, "setAlign"), "declares setAlign");
        check(declares(c, "getNoShade"), "declares getNoShade");
        check(declares(c, "setNoShade"), "declares setNoShade");
        check(declares(c, "getSize"), "declares getSize");
        check(declares(c, "setSize"), "declares setSize");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLHRElement.class), "HTMLHRElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLHRElement.class.isInterface(), "HTMLHRElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLHRElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 8, "HTMLHRElement declares exactly 8 methods");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getNoShade"), "getNoShade declared");
        check(names.contains("getSize"), "getSize declared");
        check(names.contains("getWidth"), "getWidth declared");
        check(names.contains("setAlign"), "setAlign declared");
        check(names.contains("setNoShade"), "setNoShade declared");
        check(names.contains("setSize"), "setSize declared");
        check(names.contains("setWidth"), "setWidth declared");
    }
}
