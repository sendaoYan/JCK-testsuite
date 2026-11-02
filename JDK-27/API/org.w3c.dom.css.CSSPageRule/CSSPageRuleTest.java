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

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.css.CSSPageRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSPageRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSPageRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSPageRule has superinterface");
        check(k.getMethod("getSelectorText") != null, "getSelectorText()");
        check(k.getMethod("setSelectorText", String.class) != null, "setSelectorText(String)");
        check(k.getMethod("getStyle") != null, "getStyle()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
