/*
 * @test
 * @summary org.w3c.dom.css.ElementCSSInlineStyle conformance: inline style.
 * @run main ElementCSSInlineStyleTest
 */

public class ElementCSSInlineStyleTest {
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
        Class<?> k = L("org.w3c.dom.css.ElementCSSInlineStyle");
        check(k.isInterface(), "org.w3c.dom.css.ElementCSSInlineStyle is an interface");
        check(k.getInterfaces().length == 0, "ElementCSSInlineStyle declares no superinterface");
        check(k.getMethod("getStyle") != null, "getStyle()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
