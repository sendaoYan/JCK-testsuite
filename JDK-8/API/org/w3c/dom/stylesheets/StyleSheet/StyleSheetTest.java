/*
 * @test
 * @summary org.w3c.dom.stylesheets.StyleSheet conformance: reflection shape checks.
 * @run main StyleSheetTest
 */

import java.lang.reflect.*;

public class StyleSheetTest {
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
        Class<?> c = Class.forName("org.w3c.dom.stylesheets.StyleSheet");
        check(c != null, "org.w3c.dom.stylesheets.StyleSheet loads");
        check(c.getName().equals("org.w3c.dom.stylesheets.StyleSheet"), "binary name is org.w3c.dom.stylesheets.StyleSheet");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.stylesheets.StyleSheet is public");
        check(c.isInterface(), "org.w3c.dom.stylesheets.StyleSheet is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "getDisabled"), "declares getDisabled");
        check(declares(c, "setDisabled"), "declares setDisabled");
        check(declares(c, "getOwnerNode"), "declares getOwnerNode");
        check(declares(c, "getParentStyleSheet"), "declares getParentStyleSheet");
        check(declares(c, "getHref"), "declares getHref");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: StyleSheet contract (metadata + disable toggle) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("StyleSheets", "2.0") && impl.getFeature("StyleSheets", "2.0") == null, "StyleSheets module unimplemented");
        final boolean[] disabled = new boolean[]{false};
        final org.w3c.dom.stylesheets.StyleSheet self = new org.w3c.dom.stylesheets.StyleSheet() {
            public String getType() { return "text/css"; }
            public boolean getDisabled() { return disabled[0]; }
            public void setDisabled(boolean b) { disabled[0] = b; }
            public org.w3c.dom.Node getOwnerNode() { return null; }
            public org.w3c.dom.stylesheets.StyleSheet getParentStyleSheet() { return this; }
            public String getHref() { return "base.css"; }
            public String getTitle() { return "base"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
        };
        check("text/css".equals(self.getType()) && "base.css".equals(self.getHref()) && "base".equals(self.getTitle()), "type/href/title metadata");
        check(!self.getDisabled(), "starts enabled");
        self.setDisabled(true);
        check(self.getDisabled(), "setDisabled toggles");
        check(self.getParentStyleSheet() == self && self.getOwnerNode() == null, "parent/owner wiring");
    }
}
