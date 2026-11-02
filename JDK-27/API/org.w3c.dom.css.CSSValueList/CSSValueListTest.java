/*
 * @test
 * @summary org.w3c.dom.css.CSSValueList conformance: value list.
 * @run main CSSValueListTest
 */

public class CSSValueListTest {
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
        Class<?> k = L("org.w3c.dom.css.CSSValueList");
        check(k.isInterface(), "org.w3c.dom.css.CSSValueList is an interface");
        check(L("org.w3c.dom.css.CSSValue").isAssignableFrom(k), "CSSValueList extends CSSValue");
        check(k.getInterfaces().length >= 1, "CSSValueList has superinterface");
        check(k.getMethod("getLength") != null, "getLength()");
        check(k.getMethod("item", int.class) != null, "item(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
