/*
 * @test
 * @summary org.w3c.dom.html.HTMLFormElement conformance: reflection shape checks.
 * @run main HTMLFormElementTest
 */

import java.lang.reflect.*;

public class HTMLFormElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLFormElement");
        check(c != null, "org.w3c.dom.html.HTMLFormElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLFormElement"), "binary name is org.w3c.dom.html.HTMLFormElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLFormElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLFormElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getElements"), "declares getElements");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "setName"), "declares setName");
        check(declares(c, "getAcceptCharset"), "declares getAcceptCharset");
        check(declares(c, "setAcceptCharset"), "declares setAcceptCharset");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLFormElement.class), "HTMLFormElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLFormElement.class.isInterface(), "HTMLFormElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLFormElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 16, "HTMLFormElement declares exactly 16 methods");
        check(names.contains("getAcceptCharset"), "getAcceptCharset declared");
        check(names.contains("getAction"), "getAction declared");
        check(names.contains("getElements"), "getElements declared");
        check(names.contains("getEnctype"), "getEnctype declared");
        check(names.contains("getLength"), "getLength declared");
        check(names.contains("getMethod"), "getMethod declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getTarget"), "getTarget declared");
        check(names.contains("reset"), "reset declared");
        check(names.contains("setAcceptCharset"), "setAcceptCharset declared");
        check(names.contains("setAction"), "setAction declared");
        check(names.contains("setEnctype"), "setEnctype declared");
        check(names.contains("setMethod"), "setMethod declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setTarget"), "setTarget declared");
        check(names.contains("submit"), "submit declared");
    }
}
