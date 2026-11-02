/*
 * @test
 * @summary org.w3c.dom.css.CSSStyleSheet conformance: stylesheet.
 * @run main CSSStyleSheetTest
 */

public class CSSStyleSheetTest {
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
        Class<?> k = L("org.w3c.dom.css.CSSStyleSheet");
        check(k.isInterface(), "org.w3c.dom.css.CSSStyleSheet is an interface");
        check(L("org.w3c.dom.stylesheets.StyleSheet").isAssignableFrom(k), "CSSStyleSheet extends StyleSheet");
        check(k.getInterfaces().length >= 1, "CSSStyleSheet has superinterface");
        check(k.getMethod("getOwnerRule") != null, "getOwnerRule()");
        check(k.getMethod("getCssRules") != null, "getCssRules()");
        check(k.getMethod("insertRule", String.class, int.class) != null, "insertRule(String, int)");
        check(k.getMethod("deleteRule", int.class) != null, "deleteRule(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
