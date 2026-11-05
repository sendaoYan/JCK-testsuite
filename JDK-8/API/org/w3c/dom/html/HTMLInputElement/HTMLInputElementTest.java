/*
 * @test
 * @summary org.w3c.dom.html.HTMLInputElement conformance: reflection shape checks.
 * @run main HTMLInputElementTest
 */

import java.lang.reflect.*;

public class HTMLInputElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLInputElement");
        check(c != null, "org.w3c.dom.html.HTMLInputElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLInputElement"), "binary name is org.w3c.dom.html.HTMLInputElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLInputElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLInputElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getDefaultValue"), "declares getDefaultValue");
        check(declares(c, "setDefaultValue"), "declares setDefaultValue");
        check(declares(c, "getDefaultChecked"), "declares getDefaultChecked");
        check(declares(c, "setDefaultChecked"), "declares setDefaultChecked");
        check(declares(c, "getForm"), "declares getForm");
        check(declares(c, "getAccept"), "declares getAccept");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLInputElement.class), "HTMLInputElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLInputElement.class.isInterface(), "HTMLInputElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLInputElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 38, "HTMLInputElement declares exactly 38 methods");
        check(names.contains("blur"), "blur declared");
        check(names.contains("click"), "click declared");
        check(names.contains("focus"), "focus declared");
        check(names.contains("getAccept"), "getAccept declared");
        check(names.contains("getAccessKey"), "getAccessKey declared");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getAlt"), "getAlt declared");
        check(names.contains("getChecked"), "getChecked declared");
        check(names.contains("getDefaultChecked"), "getDefaultChecked declared");
        check(names.contains("getDefaultValue"), "getDefaultValue declared");
        check(names.contains("getDisabled"), "getDisabled declared");
        check(names.contains("getForm"), "getForm declared");
        check(names.contains("getMaxLength"), "getMaxLength declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getReadOnly"), "getReadOnly declared");
        check(names.contains("getSize"), "getSize declared");
        check(names.contains("getSrc"), "getSrc declared");
        check(names.contains("getTabIndex"), "getTabIndex declared");
        check(names.contains("getType"), "getType declared");
        check(names.contains("getUseMap"), "getUseMap declared");
        check(names.contains("getValue"), "getValue declared");
        check(names.contains("select"), "select declared");
        check(names.contains("setAccept"), "setAccept declared");
        check(names.contains("setAccessKey"), "setAccessKey declared");
        check(names.contains("setAlign"), "setAlign declared");
        check(names.contains("setAlt"), "setAlt declared");
        check(names.contains("setChecked"), "setChecked declared");
        check(names.contains("setDefaultChecked"), "setDefaultChecked declared");
        check(names.contains("setDefaultValue"), "setDefaultValue declared");
        check(names.contains("setDisabled"), "setDisabled declared");
        check(names.contains("setMaxLength"), "setMaxLength declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setReadOnly"), "setReadOnly declared");
        check(names.contains("setSize"), "setSize declared");
        check(names.contains("setSrc"), "setSrc declared");
        check(names.contains("setTabIndex"), "setTabIndex declared");
        check(names.contains("setUseMap"), "setUseMap declared");
        check(names.contains("setValue"), "setValue declared");
    }
}
