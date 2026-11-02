/*
 * @test
 * @summary org.w3c.dom.css.CSSValue conformance: value type constants.
 * @run main CSSValueTest
 */

public class CSSValueTest {
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
        Class<?> k = L("org.w3c.dom.css.CSSValue");
        check(k.isInterface(), "org.w3c.dom.css.CSSValue is an interface");
        check(k.getInterfaces().length == 0, "CSSValue declares no superinterface");
        check(sconst("org.w3c.dom.css.CSSValue", "CSS_INHERIT") == 0, "CSS_INHERIT == 0");
        check(sconst("org.w3c.dom.css.CSSValue", "CSS_PRIMITIVE_VALUE") == 1, "CSS_PRIMITIVE_VALUE == 1");
        check(sconst("org.w3c.dom.css.CSSValue", "CSS_VALUE_LIST") == 2, "CSS_VALUE_LIST == 2");
        check(sconst("org.w3c.dom.css.CSSValue", "CSS_CUSTOM") == 3, "CSS_CUSTOM == 3");
        check(k.getMethod("getCssText") != null, "getCssText()");
        check(k.getMethod("setCssText", String.class) != null, "setCssText(String)");
        check(k.getMethod("getCssValueType") != null, "getCssValueType()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
