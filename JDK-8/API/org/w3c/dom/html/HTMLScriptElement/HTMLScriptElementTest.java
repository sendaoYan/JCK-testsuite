/*
 * @test
 * @summary org.w3c.dom.html.HTMLScriptElement conformance: reflection shape checks.
 * @run main HTMLScriptElementTest
 */

import java.lang.reflect.*;

public class HTMLScriptElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLScriptElement");
        check(c != null, "org.w3c.dom.html.HTMLScriptElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLScriptElement"), "binary name is org.w3c.dom.html.HTMLScriptElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLScriptElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLScriptElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getText"), "declares getText");
        check(declares(c, "setText"), "declares setText");
        check(declares(c, "getHtmlFor"), "declares getHtmlFor");
        check(declares(c, "setHtmlFor"), "declares setHtmlFor");
        check(declares(c, "getEvent"), "declares getEvent");
        check(declares(c, "setEvent"), "declares setEvent");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLScriptElement.class), "HTMLScriptElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLScriptElement.class.isInterface(), "HTMLScriptElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLScriptElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 14, "HTMLScriptElement declares exactly 14 methods");
        check(names.contains("getCharset"), "getCharset declared");
        check(names.contains("getDefer"), "getDefer declared");
        check(names.contains("getEvent"), "getEvent declared");
        check(names.contains("getHtmlFor"), "getHtmlFor declared");
        check(names.contains("getSrc"), "getSrc declared");
        check(names.contains("getText"), "getText declared");
        check(names.contains("getType"), "getType declared");
        check(names.contains("setCharset"), "setCharset declared");
        check(names.contains("setDefer"), "setDefer declared");
        check(names.contains("setEvent"), "setEvent declared");
        check(names.contains("setHtmlFor"), "setHtmlFor declared");
        check(names.contains("setSrc"), "setSrc declared");
        check(names.contains("setText"), "setText declared");
        check(names.contains("setType"), "setType declared");
    }
}
