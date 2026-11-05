/*
 * @test
 * @summary org.w3c.dom.html.HTMLLinkElement conformance: reflection shape checks.
 * @run main HTMLLinkElementTest
 */

import java.lang.reflect.*;

public class HTMLLinkElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLLinkElement");
        check(c != null, "org.w3c.dom.html.HTMLLinkElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLLinkElement"), "binary name is org.w3c.dom.html.HTMLLinkElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLLinkElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLLinkElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getDisabled"), "declares getDisabled");
        check(declares(c, "setDisabled"), "declares setDisabled");
        check(declares(c, "getCharset"), "declares getCharset");
        check(declares(c, "setCharset"), "declares setCharset");
        check(declares(c, "getHref"), "declares getHref");
        check(declares(c, "setHref"), "declares setHref");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLLinkElement.class), "HTMLLinkElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLLinkElement.class.isInterface(), "HTMLLinkElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLLinkElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 18, "HTMLLinkElement declares exactly 18 methods");
        check(names.contains("getCharset"), "getCharset declared");
        check(names.contains("getDisabled"), "getDisabled declared");
        check(names.contains("getHref"), "getHref declared");
        check(names.contains("getHreflang"), "getHreflang declared");
        check(names.contains("getMedia"), "getMedia declared");
        check(names.contains("getRel"), "getRel declared");
        check(names.contains("getRev"), "getRev declared");
        check(names.contains("getTarget"), "getTarget declared");
        check(names.contains("getType"), "getType declared");
        check(names.contains("setCharset"), "setCharset declared");
        check(names.contains("setDisabled"), "setDisabled declared");
        check(names.contains("setHref"), "setHref declared");
        check(names.contains("setHreflang"), "setHreflang declared");
        check(names.contains("setMedia"), "setMedia declared");
        check(names.contains("setRel"), "setRel declared");
        check(names.contains("setRev"), "setRev declared");
        check(names.contains("setTarget"), "setTarget declared");
        check(names.contains("setType"), "setType declared");
    }
}
