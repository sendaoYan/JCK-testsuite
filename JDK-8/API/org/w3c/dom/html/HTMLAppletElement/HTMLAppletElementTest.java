/*
 * @test
 * @summary org.w3c.dom.html.HTMLAppletElement conformance: reflection shape checks.
 * @run main HTMLAppletElementTest
 */

import java.lang.reflect.*;

public class HTMLAppletElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLAppletElement");
        check(c != null, "org.w3c.dom.html.HTMLAppletElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLAppletElement"), "binary name is org.w3c.dom.html.HTMLAppletElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLAppletElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLAppletElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getAlign"), "declares getAlign");
        check(declares(c, "setAlign"), "declares setAlign");
        check(declares(c, "getAlt"), "declares getAlt");
        check(declares(c, "setAlt"), "declares setAlt");
        check(declares(c, "getArchive"), "declares getArchive");
        check(declares(c, "setArchive"), "declares setArchive");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLAppletElement.class), "HTMLAppletElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLAppletElement.class.isInterface(), "HTMLAppletElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLAppletElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 22, "HTMLAppletElement declares exactly 22 methods");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getAlt"), "getAlt declared");
        check(names.contains("getArchive"), "getArchive declared");
        check(names.contains("getCode"), "getCode declared");
        check(names.contains("getCodeBase"), "getCodeBase declared");
        check(names.contains("getHeight"), "getHeight declared");
        check(names.contains("getHspace"), "getHspace declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getObject"), "getObject declared");
        check(names.contains("getVspace"), "getVspace declared");
        check(names.contains("getWidth"), "getWidth declared");
        check(names.contains("setAlign"), "setAlign declared");
        check(names.contains("setAlt"), "setAlt declared");
        check(names.contains("setArchive"), "setArchive declared");
        check(names.contains("setCode"), "setCode declared");
        check(names.contains("setCodeBase"), "setCodeBase declared");
        check(names.contains("setHeight"), "setHeight declared");
        check(names.contains("setHspace"), "setHspace declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setObject"), "setObject declared");
        check(names.contains("setVspace"), "setVspace declared");
        check(names.contains("setWidth"), "setWidth declared");
    }
}
