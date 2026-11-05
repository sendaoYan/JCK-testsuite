/*
 * @test
 * @summary org.w3c.dom.css.CSSFontFaceRule conformance: reflection shape checks.
 * @run main CSSFontFaceRuleTest
 */

import java.lang.reflect.*;

public class CSSFontFaceRuleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSFontFaceRule");
        check(c != null, "org.w3c.dom.css.CSSFontFaceRule loads");
        check(c.getName().equals("org.w3c.dom.css.CSSFontFaceRule"), "binary name is org.w3c.dom.css.CSSFontFaceRule");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSFontFaceRule is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSFontFaceRule is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.css.CSSRule")), "direct superinterfaces");
        check(declares(c, "getStyle"), "declares getStyle");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSFontFaceRule contract (style declaration accessor) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.class.isAssignableFrom(org.w3c.dom.css.CSSFontFaceRule.class), "CSSFontFaceRule extends CSSRule");
        org.w3c.dom.css.CSSStyleDeclaration decl = new org.w3c.dom.css.CSSStyleDeclaration() {
            String text = "color: red";
            public String getCssText() { return text; }
            public void setCssText(String t) { text = t; }
            public String getPropertyValue(String p) { return "red"; }
            public org.w3c.dom.css.CSSValue getPropertyCSSValue(String p) { return null; }
            public String removeProperty(String p) { return "red"; }
            public String getPropertyPriority(String p) { return "important"; }
            public void setProperty(String p, String v, String pri) { }
            public int getLength() { return 1; }
            public String item(int i) { return "color"; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        org.w3c.dom.css.CSSFontFaceRule r = new org.w3c.dom.css.CSSFontFaceRule() {
            public org.w3c.dom.css.CSSStyleDeclaration getStyle() { return decl; }
            public String getCssText() { return null; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.FONT_FACE_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(r.getStyle() == decl && "red".equals(r.getStyle().getPropertyValue("color")), "getStyle returns the wired declaration");
        check(r.getType() == org.w3c.dom.css.CSSRule.FONT_FACE_RULE, "rule reports FONT_FACE_RULE(5)");
    }
}
