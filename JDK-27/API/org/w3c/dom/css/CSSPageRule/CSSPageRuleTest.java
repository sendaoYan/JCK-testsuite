/*
 * @test
 * @summary org.w3c.dom.css.CSSPageRule conformance: page rule.
 * @run main CSSPageRuleTest
 */

public class CSSPageRuleTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSPageRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSPageRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSPageRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSPageRule has superinterface");
        check(k.getMethod("getSelectorText") != null, "getSelectorText()");
        check(k.getMethod("setSelectorText", String.class) != null, "setSelectorText(String)");
        check(k.getMethod("getStyle") != null, "getStyle()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSPageRule a = new org.w3c.dom.css.CSSPageRule(){
            public java.lang.String getSelectorText() { return "s"; }
            public void setSelectorText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleDeclaration getStyle() { return null; }
            public short getType() { return (short)2; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSPageRule");
        check("s".equals(a.getSelectorText()), "getSelectorText() returns fixed default");
        a.setSelectorText("s");
        check(a.getStyle() == null, "getStyle() returns fixed default");
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
