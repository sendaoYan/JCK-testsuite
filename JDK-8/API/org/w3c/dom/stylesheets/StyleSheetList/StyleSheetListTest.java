/*
 * @test
 * @summary org.w3c.dom.stylesheets.StyleSheetList conformance: reflection shape checks.
 * @run main StyleSheetListTest
 */

import java.lang.reflect.*;

public class StyleSheetListTest {
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
        Class<?> c = Class.forName("org.w3c.dom.stylesheets.StyleSheetList");
        check(c != null, "org.w3c.dom.stylesheets.StyleSheetList loads");
        check(c.getName().equals("org.w3c.dom.stylesheets.StyleSheetList"), "binary name is org.w3c.dom.stylesheets.StyleSheetList");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.stylesheets.StyleSheetList is public");
        check(c.isInterface(), "org.w3c.dom.stylesheets.StyleSheetList is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "item"), "declares item");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: StyleSheetList contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("StyleSheets", "2.0") && impl.getFeature("StyleSheets", "2.0") == null, "StyleSheets module unimplemented");
        org.w3c.dom.stylesheets.StyleSheetList l = new org.w3c.dom.stylesheets.StyleSheetList() {
            public int getLength() { return 2; }
            public org.w3c.dom.stylesheets.StyleSheet item(int i) { return null; }
        };
        check(l.getLength() == 2 && l.item(0) == null && l.item(1) == null, "length/item wiring");
        check(l.item(2) == null, "out-of-range item -> null");
    }
}
