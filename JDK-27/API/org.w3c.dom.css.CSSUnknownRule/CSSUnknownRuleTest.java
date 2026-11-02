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

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.css.CSSUnknownRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSUnknownRule is an interface");
        check(L("org.w3c.dom.css.CSSRule").isAssignableFrom(k), "CSSUnknownRule extends CSSRule");
        check(k.getInterfaces().length >= 1, "CSSUnknownRule has superinterface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
