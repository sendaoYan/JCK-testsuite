/*
 * @test
 * @summary org.w3c.dom.css.CSSMediaRule conformance: media rule.
 * @run main CSSMediaRuleTest
 */

public class CSSMediaRuleTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSMediaRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSMediaRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSMediaRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSMediaRule has superinterface");
        check(k.getMethod("getMedia") != null, "getMedia()");
        check(k.getMethod("getCssRules") != null, "getCssRules()");
        check(k.getMethod("insertRule", String.class, int.class) != null, "insertRule(String, int)");
        check(k.getMethod("deleteRule", int.class) != null, "deleteRule(int)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSMediaRule a = new org.w3c.dom.css.CSSMediaRule(){
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
            public org.w3c.dom.css.CSSRuleList getCssRules() { return null; }
            public int insertRule(java.lang.String p0, int p1) { return 3; }
            public void deleteRule(int p0) { }
            public short getType() { return (short)2; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSMediaRule");
        check(a.getMedia() == null, "getMedia() returns fixed default");
        check(a.getCssRules() == null, "getCssRules() returns fixed default");
        check(a.insertRule("s", 3) == 3, "insertRule() returns fixed default");
        a.deleteRule(3);
        check(a.getType() == (short)2, "getType() returns fixed default");
        check("s".equals(a.getCssText()), "getCssText() returns fixed default");
        a.setCssText("s");
        check(a.getParentStyleSheet() == null, "getParentStyleSheet() returns fixed default");
        check(a.getParentRule() == null, "getParentRule() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
