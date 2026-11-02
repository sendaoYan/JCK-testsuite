/*
 * @test
 * @summary org.w3c.dom.css.CSSImportRule conformance: import rule.
 * @run main CSSImportRuleTest
 */

public class CSSImportRuleTest {
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
        Class<?> k = L("org.w3c.dom.css.CSSImportRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSImportRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSImportRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSImportRule has superinterface");
        check(k.getMethod("getHref") != null, "getHref()");
        check(k.getMethod("getMedia") != null, "getMedia()");
        check(k.getMethod("getStyleSheet") != null, "getStyleSheet()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
