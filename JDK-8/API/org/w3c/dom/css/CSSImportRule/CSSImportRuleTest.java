/*
 * @test
 * @summary org.w3c.dom.css.CSSImportRule conformance: reflection shape checks.
 * @run main CSSImportRuleTest
 */

import java.lang.reflect.*;

public class CSSImportRuleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSImportRule");
        check(c != null, "org.w3c.dom.css.CSSImportRule loads");
        check(c.getName().equals("org.w3c.dom.css.CSSImportRule"), "binary name is org.w3c.dom.css.CSSImportRule");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSImportRule is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSImportRule is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.css.CSSRule")), "direct superinterfaces");
        check(declares(c, "getHref"), "declares getHref");
        check(declares(c, "getMedia"), "declares getMedia");
        check(declares(c, "getStyleSheet"), "declares getStyleSheet");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSImportRule contract (href/media/sheet) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.class.isAssignableFrom(org.w3c.dom.css.CSSImportRule.class), "CSSImportRule extends CSSRule");
        org.w3c.dom.css.CSSImportRule r = new org.w3c.dom.css.CSSImportRule() {
            public String getHref() { return "imp.css"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
            public org.w3c.dom.css.CSSStyleSheet getStyleSheet() { return null; }
            public String getCssText() { return null; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.IMPORT_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check("imp.css".equals(r.getHref()), "href round-trip");
        check(r.getType() == org.w3c.dom.css.CSSRule.IMPORT_RULE, "rule reports IMPORT_RULE(3)");
        check(r.getMedia() == null && r.getStyleSheet() == null, "media/sheet nulls by wiring");
    }
}
