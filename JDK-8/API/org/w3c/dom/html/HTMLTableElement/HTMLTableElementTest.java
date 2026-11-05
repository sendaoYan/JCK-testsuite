/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableElement conformance: reflection shape checks.
 * @run main HTMLTableElementTest
 */

import java.lang.reflect.*;

public class HTMLTableElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLTableElement");
        check(c != null, "org.w3c.dom.html.HTMLTableElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLTableElement"), "binary name is org.w3c.dom.html.HTMLTableElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLTableElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLTableElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getCaption"), "declares getCaption");
        check(declares(c, "setCaption"), "declares setCaption");
        check(declares(c, "getTHead"), "declares getTHead");
        check(declares(c, "setTHead"), "declares setTHead");
        check(declares(c, "getTFoot"), "declares getTFoot");
        check(declares(c, "setTFoot"), "declares setTFoot");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLTableElement.class), "HTMLTableElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLTableElement.class.isInterface(), "HTMLTableElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLTableElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 34, "HTMLTableElement declares exactly 34 methods");
        check(names.contains("createCaption"), "createCaption declared");
        check(names.contains("createTFoot"), "createTFoot declared");
        check(names.contains("createTHead"), "createTHead declared");
        check(names.contains("deleteCaption"), "deleteCaption declared");
        check(names.contains("deleteRow"), "deleteRow declared");
        check(names.contains("deleteTFoot"), "deleteTFoot declared");
        check(names.contains("deleteTHead"), "deleteTHead declared");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getBgColor"), "getBgColor declared");
        check(names.contains("getBorder"), "getBorder declared");
        check(names.contains("getCaption"), "getCaption declared");
        check(names.contains("getCellPadding"), "getCellPadding declared");
        check(names.contains("getCellSpacing"), "getCellSpacing declared");
        check(names.contains("getFrame"), "getFrame declared");
        check(names.contains("getRows"), "getRows declared");
        check(names.contains("getRules"), "getRules declared");
        check(names.contains("getSummary"), "getSummary declared");
        check(names.contains("getTBodies"), "getTBodies declared");
        check(names.contains("getTFoot"), "getTFoot declared");
        check(names.contains("getTHead"), "getTHead declared");
        check(names.contains("getWidth"), "getWidth declared");
        check(names.contains("insertRow"), "insertRow declared");
        check(names.contains("setAlign"), "setAlign declared");
        check(names.contains("setBgColor"), "setBgColor declared");
        check(names.contains("setBorder"), "setBorder declared");
        check(names.contains("setCaption"), "setCaption declared");
        check(names.contains("setCellPadding"), "setCellPadding declared");
        check(names.contains("setCellSpacing"), "setCellSpacing declared");
        check(names.contains("setFrame"), "setFrame declared");
        check(names.contains("setRules"), "setRules declared");
        check(names.contains("setSummary"), "setSummary declared");
        check(names.contains("setTFoot"), "setTFoot declared");
        check(names.contains("setTHead"), "setTHead declared");
        check(names.contains("setWidth"), "setWidth declared");
    }
}
