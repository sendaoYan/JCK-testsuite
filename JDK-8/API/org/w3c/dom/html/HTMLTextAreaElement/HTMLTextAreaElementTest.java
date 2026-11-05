/*
 * @test
 * @summary org.w3c.dom.html.HTMLTextAreaElement conformance: reflection shape checks.
 * @run main HTMLTextAreaElementTest
 */

import java.lang.reflect.*;

public class HTMLTextAreaElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLTextAreaElement");
        check(c != null, "org.w3c.dom.html.HTMLTextAreaElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLTextAreaElement"), "binary name is org.w3c.dom.html.HTMLTextAreaElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLTextAreaElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLTextAreaElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getDefaultValue"), "declares getDefaultValue");
        check(declares(c, "setDefaultValue"), "declares setDefaultValue");
        check(declares(c, "getForm"), "declares getForm");
        check(declares(c, "getAccessKey"), "declares getAccessKey");
        check(declares(c, "setAccessKey"), "declares setAccessKey");
        check(declares(c, "getCols"), "declares getCols");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLTextAreaElement.class), "HTMLTextAreaElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLTextAreaElement.class.isInterface(), "HTMLTextAreaElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLTextAreaElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 23, "HTMLTextAreaElement declares exactly 23 methods");
        check(names.contains("blur"), "blur declared");
        check(names.contains("focus"), "focus declared");
        check(names.contains("getAccessKey"), "getAccessKey declared");
        check(names.contains("getCols"), "getCols declared");
        check(names.contains("getDefaultValue"), "getDefaultValue declared");
        check(names.contains("getDisabled"), "getDisabled declared");
        check(names.contains("getForm"), "getForm declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getReadOnly"), "getReadOnly declared");
        check(names.contains("getRows"), "getRows declared");
        check(names.contains("getTabIndex"), "getTabIndex declared");
        check(names.contains("getType"), "getType declared");
        check(names.contains("getValue"), "getValue declared");
        check(names.contains("select"), "select declared");
        check(names.contains("setAccessKey"), "setAccessKey declared");
        check(names.contains("setCols"), "setCols declared");
        check(names.contains("setDefaultValue"), "setDefaultValue declared");
        check(names.contains("setDisabled"), "setDisabled declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setReadOnly"), "setReadOnly declared");
        check(names.contains("setRows"), "setRows declared");
        check(names.contains("setTabIndex"), "setTabIndex declared");
        check(names.contains("setValue"), "setValue declared");
    }
}
