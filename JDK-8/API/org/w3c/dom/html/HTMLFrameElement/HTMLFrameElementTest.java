/*
 * @test
 * @summary org.w3c.dom.html.HTMLFrameElement conformance: reflection shape checks.
 * @run main HTMLFrameElementTest
 */

import java.lang.reflect.*;

public class HTMLFrameElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLFrameElement");
        check(c != null, "org.w3c.dom.html.HTMLFrameElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLFrameElement"), "binary name is org.w3c.dom.html.HTMLFrameElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLFrameElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLFrameElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getFrameBorder"), "declares getFrameBorder");
        check(declares(c, "setFrameBorder"), "declares setFrameBorder");
        check(declares(c, "getLongDesc"), "declares getLongDesc");
        check(declares(c, "setLongDesc"), "declares setLongDesc");
        check(declares(c, "getMarginHeight"), "declares getMarginHeight");
        check(declares(c, "setMarginHeight"), "declares setMarginHeight");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLFrameElement.class), "HTMLFrameElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLFrameElement.class.isInterface(), "HTMLFrameElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLFrameElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 17, "HTMLFrameElement declares exactly 17 methods");
        check(names.contains("getContentDocument"), "getContentDocument declared");
        check(names.contains("getFrameBorder"), "getFrameBorder declared");
        check(names.contains("getLongDesc"), "getLongDesc declared");
        check(names.contains("getMarginHeight"), "getMarginHeight declared");
        check(names.contains("getMarginWidth"), "getMarginWidth declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getNoResize"), "getNoResize declared");
        check(names.contains("getScrolling"), "getScrolling declared");
        check(names.contains("getSrc"), "getSrc declared");
        check(names.contains("setFrameBorder"), "setFrameBorder declared");
        check(names.contains("setLongDesc"), "setLongDesc declared");
        check(names.contains("setMarginHeight"), "setMarginHeight declared");
        check(names.contains("setMarginWidth"), "setMarginWidth declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setNoResize"), "setNoResize declared");
        check(names.contains("setScrolling"), "setScrolling declared");
        check(names.contains("setSrc"), "setSrc declared");
    }
}
