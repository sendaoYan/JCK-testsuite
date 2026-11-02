/*
 * @test
 * @summary org.w3c.dom.css.CSSFontFaceRule conformance: font-face rule.
 * @run main CSSFontFaceRuleTest
 */

public class CSSFontFaceRuleTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.css.CSSFontFaceRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSFontFaceRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSFontFaceRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSFontFaceRule has superinterface");
        check(k.getMethod("getStyle") != null, "getStyle()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
