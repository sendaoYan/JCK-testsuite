/*
 * @test
 * @summary org.w3c.dom.html.HTMLDocument conformance: reflection shape checks.
 * @run main HTMLDocumentTest
 */

import java.lang.reflect.*;

public class HTMLDocumentTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLDocument");
        check(c != null, "org.w3c.dom.html.HTMLDocument loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLDocument"), "binary name is org.w3c.dom.html.HTMLDocument");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLDocument is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLDocument is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Document")), "direct superinterfaces");
        check(declares(c, "getTitle"), "declares getTitle");
        check(declares(c, "setTitle"), "declares setTitle");
        check(declares(c, "getReferrer"), "declares getReferrer");
        check(declares(c, "getDomain"), "declares getDomain");
        check(declares(c, "getURL"), "declares getURL");
        check(declares(c, "getBody"), "declares getBody");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.Document.class.isAssignableFrom(org.w3c.dom.html.HTMLDocument.class), "HTMLDocument extends org.w3c.dom.Document");
        check(org.w3c.dom.html.HTMLDocument.class.isInterface(), "HTMLDocument is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLDocument.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 19, "HTMLDocument declares exactly 19 methods");
        check(names.contains("close"), "close declared");
        check(names.contains("getAnchors"), "getAnchors declared");
        check(names.contains("getApplets"), "getApplets declared");
        check(names.contains("getBody"), "getBody declared");
        check(names.contains("getCookie"), "getCookie declared");
        check(names.contains("getDomain"), "getDomain declared");
        check(names.contains("getElementsByName"), "getElementsByName declared");
        check(names.contains("getForms"), "getForms declared");
        check(names.contains("getImages"), "getImages declared");
        check(names.contains("getLinks"), "getLinks declared");
        check(names.contains("getReferrer"), "getReferrer declared");
        check(names.contains("getTitle"), "getTitle declared");
        check(names.contains("getURL"), "getURL declared");
        check(names.contains("open"), "open declared");
        check(names.contains("setBody"), "setBody declared");
        check(names.contains("setCookie"), "setCookie declared");
        check(names.contains("setTitle"), "setTitle declared");
        check(names.contains("write"), "write declared");
        check(names.contains("writeln"), "writeln declared");
    }
}
