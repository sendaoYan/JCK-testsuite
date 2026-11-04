/*
 * @test
 * @summary org.w3c.dom.css.CSSCharsetRule conformance: charset rule.
 * @run main CSSCharsetRuleTest
 */

public class CSSCharsetRuleTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSCharsetRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSCharsetRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSCharsetRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSCharsetRule has superinterface");
        check(k.getMethod("getEncoding") != null, "getEncoding()");
        check(k.getMethod("setEncoding", String.class) != null, "setEncoding(String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSCharsetRule a = new org.w3c.dom.css.CSSCharsetRule(){
            public java.lang.String getEncoding() { return "s"; }
            public void setEncoding(java.lang.String p0) { }
            public short getType() { return (short)2; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSCharsetRule");
        check("s".equals(a.getEncoding()), "getEncoding() returns fixed default");
        a.setEncoding("s");
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
