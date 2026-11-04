/*
 * @test
 * @summary org.w3c.dom.css.CSSStyleSheet conformance: stylesheet.
 * @run main CSSStyleSheetTest
 */

public class CSSStyleSheetTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.css.CSSStyleSheet");
        check(k.isInterface(), "org.w3c.dom.css.CSSStyleSheet is an interface");
        check(L("org.w3c.dom.stylesheets.StyleSheet").isAssignableFrom(k), "CSSStyleSheet extends StyleSheet");
        check(k.getInterfaces().length >= 1, "CSSStyleSheet has superinterface");
        check(k.getMethod("getOwnerRule") != null, "getOwnerRule()");
        check(k.getMethod("getCssRules") != null, "getCssRules()");
        check(k.getMethod("insertRule", String.class, int.class) != null, "insertRule(String, int)");
        check(k.getMethod("deleteRule", int.class) != null, "deleteRule(int)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSStyleSheet a = new org.w3c.dom.css.CSSStyleSheet(){
            public org.w3c.dom.css.CSSRule getOwnerRule() { return null; }
            public org.w3c.dom.css.CSSRuleList getCssRules() { return null; }
            public int insertRule(java.lang.String p0, int p1) { return 3; }
            public void deleteRule(int p0) { }
            public java.lang.String getType() { return "s"; }
            public boolean getDisabled() { return true; }
            public void setDisabled(boolean p0) { }
            public org.w3c.dom.Node getOwnerNode() { return null; }
            public org.w3c.dom.stylesheets.StyleSheet getParentStyleSheet() { return null; }
            public java.lang.String getHref() { return "s"; }
            public java.lang.String getTitle() { return "s"; }
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
        };
        check(a != null, "anon impl of CSSStyleSheet");
        check(a.getOwnerRule() == null, "getOwnerRule() returns fixed default");
        check(a.getCssRules() == null, "getCssRules() returns fixed default");
        check(a.insertRule("s", 3) == 3, "insertRule() returns fixed default");
        a.deleteRule(3);
        check("s".equals(a.getType()), "getType() returns fixed default");
        check(a.getDisabled() == true, "getDisabled() returns fixed default");
        a.setDisabled(true);
        check(a.getOwnerNode() == null, "getOwnerNode() returns fixed default");
        check(a.getParentStyleSheet() == null, "getParentStyleSheet() returns fixed default");
        check("s".equals(a.getHref()), "getHref() returns fixed default");
        check("s".equals(a.getTitle()), "getTitle() returns fixed default");
        check(a.getMedia() == null, "getMedia() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
