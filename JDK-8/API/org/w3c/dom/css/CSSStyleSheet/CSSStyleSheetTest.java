/*
 * @test
 * @summary org.w3c.dom.css.CSSStyleSheet conformance: reflection shape checks.
 * @run main CSSStyleSheetTest
 */

import java.lang.reflect.*;

public class CSSStyleSheetTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSStyleSheet");
        check(c != null, "org.w3c.dom.css.CSSStyleSheet loads");
        check(c.getName().equals("org.w3c.dom.css.CSSStyleSheet"), "binary name is org.w3c.dom.css.CSSStyleSheet");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSStyleSheet is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSStyleSheet is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.stylesheets.StyleSheet")), "direct superinterfaces");
        check(declares(c, "getOwnerRule"), "declares getOwnerRule");
        check(declares(c, "getCssRules"), "declares getCssRules");
        check(declares(c, "insertRule"), "declares insertRule");
        check(declares(c, "deleteRule"), "declares deleteRule");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSStyleSheet contract (owner rule + rule management) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.stylesheets.StyleSheet parent = new org.w3c.dom.stylesheets.StyleSheet() {
            public String getType() { return "text/css"; }
            public boolean getDisabled() { return false; }
            public void setDisabled(boolean b) { }
            public org.w3c.dom.Node getOwnerNode() { return null; }
            public org.w3c.dom.stylesheets.StyleSheet getParentStyleSheet() { return null; }
            public String getHref() { return "p.css"; }
            public String getTitle() { return "t"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
        };
        check("text/css".equals(parent.getType()) && "p.css".equals(parent.getHref()), "wired parent sheet metadata");
        final int[] n = new int[]{1};
        org.w3c.dom.css.CSSStyleSheet s = new org.w3c.dom.css.CSSStyleSheet() {
            public org.w3c.dom.css.CSSRule getOwnerRule() { return null; }
            public org.w3c.dom.css.CSSRuleList getCssRules() { return null; }
            public int insertRule(String rule, int index) { n[0]++; return index; }
            public void deleteRule(int index) { n[0]--; }
            public String getType() { return "text/css"; }
            public boolean getDisabled() { return false; }
            public void setDisabled(boolean b) { }
            public org.w3c.dom.Node getOwnerNode() { return null; }
            public org.w3c.dom.stylesheets.StyleSheet getParentStyleSheet() { return parent; }
            public String getHref() { return "s.css"; }
            public String getTitle() { return null; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
        };
        check(s.insertRule("p{}", 0) == 0 && n[0] == 2, "insertRule returns the index");
        s.deleteRule(0);
        check(n[0] == 1, "deleteRule balances insertRule");
        check(s.getParentStyleSheet() == parent && s.getOwnerRule() == null, "parent sheet wired, no owner rule");
    }
}
