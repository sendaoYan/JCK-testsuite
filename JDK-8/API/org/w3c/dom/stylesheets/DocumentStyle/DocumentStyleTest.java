/*
 * @test
 * @summary org.w3c.dom.stylesheets.DocumentStyle conformance: reflection shape checks.
 * @run main DocumentStyleTest
 */

import java.lang.reflect.*;

public class DocumentStyleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.stylesheets.DocumentStyle");
        check(c != null, "org.w3c.dom.stylesheets.DocumentStyle loads");
        check(c.getName().equals("org.w3c.dom.stylesheets.DocumentStyle"), "binary name is org.w3c.dom.stylesheets.DocumentStyle");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.stylesheets.DocumentStyle is public");
        check(c.isInterface(), "org.w3c.dom.stylesheets.DocumentStyle is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getStyleSheets"), "declares getStyleSheets");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: DocumentStyle contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("StyleSheets", "2.0") && impl.getFeature("StyleSheets", "2.0") == null, "StyleSheets module unimplemented");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        check(!org.w3c.dom.stylesheets.DocumentStyle.class.isInstance(d), "Document is not a DocumentStyle (module absent)");
        org.w3c.dom.stylesheets.StyleSheetList empty = new org.w3c.dom.stylesheets.StyleSheetList() {
            public int getLength() { return 0; }
            public org.w3c.dom.stylesheets.StyleSheet item(int i) { return null; }
        };
        org.w3c.dom.stylesheets.DocumentStyle ds = new org.w3c.dom.stylesheets.DocumentStyle() {
            public org.w3c.dom.stylesheets.StyleSheetList getStyleSheets() { return empty; }
        };
        check(ds.getStyleSheets() == empty && empty.getLength() == 0 && empty.item(0) == null, "styleSheets list routed");
    }
}
