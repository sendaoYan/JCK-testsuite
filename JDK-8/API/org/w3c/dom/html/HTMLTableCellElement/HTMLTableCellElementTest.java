/*
 * @test
 * @summary org.w3c.dom.html.HTMLTableCellElement conformance: reflection shape checks.
 * @run main HTMLTableCellElementTest
 */

import java.lang.reflect.*;

public class HTMLTableCellElementTest {
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
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLTableCellElement");
        check(c != null, "org.w3c.dom.html.HTMLTableCellElement loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLTableCellElement"), "binary name is org.w3c.dom.html.HTMLTableCellElement");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLTableCellElement is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLTableCellElement is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.html.HTMLElement")), "direct superinterfaces");
        check(declares(c, "getCellIndex"), "declares getCellIndex");
        check(declares(c, "getAbbr"), "declares getAbbr");
        check(declares(c, "setAbbr"), "declares setAbbr");
        check(declares(c, "getAlign"), "declares getAlign");
        check(declares(c, "setAlign"), "declares setAlign");
        check(declares(c, "getAxis"), "declares getAxis");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLTableCellElement.class), "HTMLTableCellElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLTableCellElement.class.isInterface(), "HTMLTableCellElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLTableCellElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 29, "HTMLTableCellElement declares exactly 29 methods");
        check(names.contains("getAbbr"), "getAbbr declared");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getAxis"), "getAxis declared");
        check(names.contains("getBgColor"), "getBgColor declared");
        check(names.contains("getCellIndex"), "getCellIndex declared");
        check(names.contains("getCh"), "getCh declared");
        check(names.contains("getChOff"), "getChOff declared");
        check(names.contains("getColSpan"), "getColSpan declared");
        check(names.contains("getHeaders"), "getHeaders declared");
        check(names.contains("getHeight"), "getHeight declared");
        check(names.contains("getNoWrap"), "getNoWrap declared");
        check(names.contains("getRowSpan"), "getRowSpan declared");
        check(names.contains("getScope"), "getScope declared");
        check(names.contains("getVAlign"), "getVAlign declared");
        check(names.contains("getWidth"), "getWidth declared");
        check(names.contains("setAbbr"), "setAbbr declared");
        check(names.contains("setAlign"), "setAlign declared");
        check(names.contains("setAxis"), "setAxis declared");
        check(names.contains("setBgColor"), "setBgColor declared");
        check(names.contains("setCh"), "setCh declared");
        check(names.contains("setChOff"), "setChOff declared");
        check(names.contains("setColSpan"), "setColSpan declared");
        check(names.contains("setHeaders"), "setHeaders declared");
        check(names.contains("setHeight"), "setHeight declared");
        check(names.contains("setNoWrap"), "setNoWrap declared");
        check(names.contains("setRowSpan"), "setRowSpan declared");
        check(names.contains("setScope"), "setScope declared");
        check(names.contains("setVAlign"), "setVAlign declared");
        check(names.contains("setWidth"), "setWidth declared");
    }
}
