/*
 * @test
 * @summary org.w3c.dom.css.CSSUnknownRule conformance: unknown rule.
 * @run main CSSUnknownRuleTest
 */

public class CSSUnknownRuleTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSUnknownRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSUnknownRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSUnknownRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSUnknownRule has superinterface");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSUnknownRule a = new org.w3c.dom.css.CSSUnknownRule(){
            public short getType() { return (short)2; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSUnknownRule");
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
