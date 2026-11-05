/*
 * @test
 * @summary org.w3c.dom.html.HTMLObjectElement conformance: reflection shape checks.
 * @run main HTMLObjectElementTest
 */

import java.lang.reflect.*;

public class HTMLObjectElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLObjectElement");
        check(c != null, "org.w3c.dom.html.HTMLObjectElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLObjectElement"), "binary name is org.w3c.dom.html.HTMLObjectElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLObjectElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLObjectElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getForm"), "declares getForm");
        check(declares(c, "getCode"), "declares getCode");
        check(declares(c, "setCode"), "declares setCode");
        check(declares(c, "getAlign"), "declares getAlign");
        check(declares(c, "setAlign"), "declares setAlign");
        check(declares(c, "getArchive"), "declares getArchive");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLObjectElement.class), "HTMLObjectElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLObjectElement.class.isInterface(), "HTMLObjectElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLObjectElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 36, "HTMLObjectElement declares exactly 36 methods");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getArchive"), "getArchive declared");
        check(names.contains("getBorder"), "getBorder declared");
        check(names.contains("getCode"), "getCode declared");
        check(names.contains("getCodeBase"), "getCodeBase declared");
        check(names.contains("getCodeType"), "getCodeType declared");
        check(names.contains("getContentDocument"), "getContentDocument declared");
        check(names.contains("getData"), "getData declared");
        check(names.contains("getDeclare"), "getDeclare declared");
        check(names.contains("getForm"), "getForm declared");
        check(names.contains("getHeight"), "getHeight declared");
        check(names.contains("getHspace"), "getHspace declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getStandby"), "getStandby declared");
        check(names.contains("getTabIndex"), "getTabIndex declared");
        check(names.contains("getType"), "getType declared");
        check(names.contains("getUseMap"), "getUseMap declared");
        check(names.contains("getVspace"), "getVspace declared");
        check(names.contains("getWidth"), "getWidth declared");
        check(names.contains("setAlign"), "setAlign declared");
        check(names.contains("setArchive"), "setArchive declared");
        check(names.contains("setBorder"), "setBorder declared");
        check(names.contains("setCode"), "setCode declared");
        check(names.contains("setCodeBase"), "setCodeBase declared");
        check(names.contains("setCodeType"), "setCodeType declared");
        check(names.contains("setData"), "setData declared");
        check(names.contains("setDeclare"), "setDeclare declared");
        check(names.contains("setHeight"), "setHeight declared");
        check(names.contains("setHspace"), "setHspace declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setStandby"), "setStandby declared");
        check(names.contains("setTabIndex"), "setTabIndex declared");
        check(names.contains("setType"), "setType declared");
        check(names.contains("setUseMap"), "setUseMap declared");
        check(names.contains("setVspace"), "setVspace declared");
        check(names.contains("setWidth"), "setWidth declared");
    }
}
