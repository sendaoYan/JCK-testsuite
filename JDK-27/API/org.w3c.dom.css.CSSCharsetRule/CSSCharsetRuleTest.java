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

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.css.CSSCharsetRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSCharsetRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSCharsetRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSCharsetRule has superinterface");
        check(k.getMethod("getEncoding") != null, "getEncoding()");
        check(k.getMethod("setEncoding", String.class) != null, "setEncoding(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
