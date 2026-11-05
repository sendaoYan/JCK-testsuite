/*
 * @test
 * @summary org.w3c.dom.html.HTMLAnchorElement conformance: reflection shape checks.
 * @run main HTMLAnchorElementTest
 */

import java.lang.reflect.*;

public class HTMLAnchorElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLAnchorElement");
        check(c != null, "org.w3c.dom.html.HTMLAnchorElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLAnchorElement"), "binary name is org.w3c.dom.html.HTMLAnchorElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLAnchorElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLAnchorElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getAccessKey"), "declares getAccessKey");
        check(declares(c, "setAccessKey"), "declares setAccessKey");
        check(declares(c, "getCharset"), "declares getCharset");
        check(declares(c, "setCharset"), "declares setCharset");
        check(declares(c, "getCoords"), "declares getCoords");
        check(declares(c, "setCoords"), "declares setCoords");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLAnchorElement.class), "HTMLAnchorElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLAnchorElement.class.isInterface(), "HTMLAnchorElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLAnchorElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 26, "HTMLAnchorElement declares exactly 26 methods");
        check(names.contains("blur"), "blur declared");
        check(names.contains("focus"), "focus declared");
        check(names.contains("getAccessKey"), "getAccessKey declared");
        check(names.contains("getCharset"), "getCharset declared");
        check(names.contains("getCoords"), "getCoords declared");
        check(names.contains("getHref"), "getHref declared");
        check(names.contains("getHreflang"), "getHreflang declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getRel"), "getRel declared");
        check(names.contains("getRev"), "getRev declared");
        check(names.contains("getShape"), "getShape declared");
        check(names.contains("getTabIndex"), "getTabIndex declared");
        check(names.contains("getTarget"), "getTarget declared");
        check(names.contains("getType"), "getType declared");
        check(names.contains("setAccessKey"), "setAccessKey declared");
        check(names.contains("setCharset"), "setCharset declared");
        check(names.contains("setCoords"), "setCoords declared");
        check(names.contains("setHref"), "setHref declared");
        check(names.contains("setHreflang"), "setHreflang declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setRel"), "setRel declared");
        check(names.contains("setRev"), "setRev declared");
        check(names.contains("setShape"), "setShape declared");
        check(names.contains("setTabIndex"), "setTabIndex declared");
        check(names.contains("setTarget"), "setTarget declared");
        check(names.contains("setType"), "setType declared");
    }
}
