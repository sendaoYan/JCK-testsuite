/*
 * @test
 * @summary org.w3c.dom.html.HTMLMetaElement conformance: reflection shape checks.
 * @run main HTMLMetaElementTest
 */

import java.lang.reflect.*;

public class HTMLMetaElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLMetaElement");
        check(c != null, "org.w3c.dom.html.HTMLMetaElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLMetaElement"), "binary name is org.w3c.dom.html.HTMLMetaElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLMetaElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLMetaElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getContent"), "declares getContent");
        check(declares(c, "setContent"), "declares setContent");
        check(declares(c, "getHttpEquiv"), "declares getHttpEquiv");
        check(declares(c, "setHttpEquiv"), "declares setHttpEquiv");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "setName"), "declares setName");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLMetaElement.class), "HTMLMetaElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLMetaElement.class.isInterface(), "HTMLMetaElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLMetaElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 8, "HTMLMetaElement declares exactly 8 methods");
        check(names.contains("getContent"), "getContent declared");
        check(names.contains("getHttpEquiv"), "getHttpEquiv declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getScheme"), "getScheme declared");
        check(names.contains("setContent"), "setContent declared");
        check(names.contains("setHttpEquiv"), "setHttpEquiv declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setScheme"), "setScheme declared");
    }
}
