/*
 * @test
 * @summary org.w3c.dom.html.HTMLSelectElement conformance: reflection shape checks.
 * @run main HTMLSelectElementTest
 */

import java.lang.reflect.*;

public class HTMLSelectElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLSelectElement");
        check(c != null, "org.w3c.dom.html.HTMLSelectElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLSelectElement"), "binary name is org.w3c.dom.html.HTMLSelectElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLSelectElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLSelectElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "getSelectedIndex"), "declares getSelectedIndex");
        check(declares(c, "setSelectedIndex"), "declares setSelectedIndex");
        check(declares(c, "getValue"), "declares getValue");
        check(declares(c, "setValue"), "declares setValue");
        check(declares(c, "getLength"), "declares getLength");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLSelectElement.class), "HTMLSelectElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLSelectElement.class.isInterface(), "HTMLSelectElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLSelectElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 22, "HTMLSelectElement declares exactly 22 methods");
        check(names.contains("add"), "add declared");
        check(names.contains("blur"), "blur declared");
        check(names.contains("focus"), "focus declared");
        check(names.contains("getDisabled"), "getDisabled declared");
        check(names.contains("getForm"), "getForm declared");
        check(names.contains("getLength"), "getLength declared");
        check(names.contains("getMultiple"), "getMultiple declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getOptions"), "getOptions declared");
        check(names.contains("getSelectedIndex"), "getSelectedIndex declared");
        check(names.contains("getSize"), "getSize declared");
        check(names.contains("getTabIndex"), "getTabIndex declared");
        check(names.contains("getType"), "getType declared");
        check(names.contains("getValue"), "getValue declared");
        check(names.contains("remove"), "remove declared");
        check(names.contains("setDisabled"), "setDisabled declared");
        check(names.contains("setMultiple"), "setMultiple declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setSelectedIndex"), "setSelectedIndex declared");
        check(names.contains("setSize"), "setSize declared");
        check(names.contains("setTabIndex"), "setTabIndex declared");
        check(names.contains("setValue"), "setValue declared");
    }
}
