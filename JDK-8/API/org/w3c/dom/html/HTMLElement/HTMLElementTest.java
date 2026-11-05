/*
 * @test
 * @summary org.w3c.dom.html.HTMLElement conformance: reflection shape checks.
 * @run main HTMLElementTest
 */

import java.lang.reflect.*;

public class HTMLElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLElement");
        check(c != null, "org.w3c.dom.html.HTMLElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLElement"), "binary name is org.w3c.dom.html.HTMLElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Element")), "direct superinterfaces");
        check(declares(c, "getId"), "declares getId");
        check(declares(c, "setId"), "declares setId");
        check(declares(c, "getTitle"), "declares getTitle");
        check(declares(c, "setTitle"), "declares setTitle");
        check(declares(c, "getLang"), "declares getLang");
        check(declares(c, "setLang"), "declares setLang");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.Element.class.isAssignableFrom(org.w3c.dom.html.HTMLElement.class), "HTMLElement extends org.w3c.dom.Element");
        check(org.w3c.dom.html.HTMLElement.class.isInterface(), "HTMLElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 10, "HTMLElement declares exactly 10 methods");
        check(names.contains("getClassName"), "getClassName declared");
        check(names.contains("getDir"), "getDir declared");
        check(names.contains("getId"), "getId declared");
        check(names.contains("getLang"), "getLang declared");
        check(names.contains("getTitle"), "getTitle declared");
        check(names.contains("setClassName"), "setClassName declared");
        check(names.contains("setDir"), "setDir declared");
        check(names.contains("setId"), "setId declared");
        check(names.contains("setLang"), "setLang declared");
        check(names.contains("setTitle"), "setTitle declared");
    }
}
