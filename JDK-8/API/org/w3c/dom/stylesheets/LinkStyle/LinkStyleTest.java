/*
 * @test
 * @summary org.w3c.dom.stylesheets.LinkStyle conformance: reflection shape checks.
 * @run main LinkStyleTest
 */

import java.lang.reflect.*;

public class LinkStyleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.stylesheets.LinkStyle");
        check(c != null, "org.w3c.dom.stylesheets.LinkStyle loads");
        check(c.getName().equals("org.w3c.dom.stylesheets.LinkStyle"), "binary name is org.w3c.dom.stylesheets.LinkStyle");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.stylesheets.LinkStyle is public");
        check(c.isInterface(), "org.w3c.dom.stylesheets.LinkStyle is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getSheet"), "declares getSheet");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: LinkStyle contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("StyleSheets", "2.0") && impl.getFeature("StyleSheets", "2.0") == null, "StyleSheets module unimplemented");
        org.w3c.dom.stylesheets.StyleSheet sheet = new org.w3c.dom.stylesheets.StyleSheet() {
            public String getType() { return "text/css"; }
            public boolean getDisabled() { return false; }
            public void setDisabled(boolean b) { }
            public org.w3c.dom.Node getOwnerNode() { return null; }
            public org.w3c.dom.stylesheets.StyleSheet getParentStyleSheet() { return null; }
            public String getHref() { return null; }
            public String getTitle() { return "linked"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
        };
        org.w3c.dom.stylesheets.LinkStyle ls = new org.w3c.dom.stylesheets.LinkStyle() {
            public org.w3c.dom.stylesheets.StyleSheet getSheet() { return sheet; }
        };
        check(ls.getSheet() == sheet && "linked".equals(ls.getSheet().getTitle()), "owning node exposes its sheet");
    }
}
