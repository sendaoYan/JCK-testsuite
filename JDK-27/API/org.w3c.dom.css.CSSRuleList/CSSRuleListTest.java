/*
 * @test
 * @summary org.w3c.dom.css.CSSRuleList conformance: ordered rule list.
 * @run main CSSRuleListTest
 */

public class CSSRuleListTest {
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
        Class<?> k = L("org.w3c.dom.css.CSSRuleList");
        check(k.isInterface(), "org.w3c.dom.css.CSSRuleList is an interface");
        check(k.getInterfaces().length == 0, "CSSRuleList declares no superinterface");
        check(k.getMethod("getLength") != null, "getLength()");
        check(k.getMethod("item", int.class) != null, "item(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
