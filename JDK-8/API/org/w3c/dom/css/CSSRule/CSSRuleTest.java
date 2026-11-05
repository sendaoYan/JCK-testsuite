/*
 * @test
 * @summary org.w3c.dom.css.CSSRule conformance: reflection shape checks.
 * @run main CSSRuleTest
 */

import java.lang.reflect.*;

public class CSSRuleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSRule");
        check(c != null, "org.w3c.dom.css.CSSRule loads");
        check(c.getName().equals("org.w3c.dom.css.CSSRule"), "binary name is org.w3c.dom.css.CSSRule");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSRule is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSRule is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "getCssText"), "declares getCssText");
        check(declares(c, "setCssText"), "declares setCssText");
        check(declares(c, "getParentStyleSheet"), "declares getParentStyleSheet");
        check(declares(c, "getParentRule"), "declares getParentRule");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSRule contract (7 rule-type constants + base shape) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.UNKNOWN_RULE == 0 && org.w3c.dom.css.CSSRule.STYLE_RULE == 1, "UNKNOWN_RULE=0 STYLE_RULE=1");
        check(org.w3c.dom.css.CSSRule.CHARSET_RULE == 2 && org.w3c.dom.css.CSSRule.IMPORT_RULE == 3, "CHARSET_RULE=2 IMPORT_RULE=3");
        check(org.w3c.dom.css.CSSRule.MEDIA_RULE == 4 && org.w3c.dom.css.CSSRule.FONT_FACE_RULE == 5, "MEDIA_RULE=4 FONT_FACE_RULE=5");
        check(org.w3c.dom.css.CSSRule.PAGE_RULE == 6, "PAGE_RULE=6");
        org.w3c.dom.css.CSSRule r = new org.w3c.dom.css.CSSRule() {
            public String getCssText() { return "@rule"; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.STYLE_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check("@rule".equals(r.getCssText()) && r.getType() == org.w3c.dom.css.CSSRule.STYLE_RULE, "anonymous base rule returns the wired values");
    }
}
