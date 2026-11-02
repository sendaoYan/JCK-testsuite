/*
 * @test
 * @summary org.w3c.dom.css.CSSStyleDeclaration conformance: style declaration.
 * @run main CSSStyleDeclarationTest
 */

public class CSSStyleDeclarationTest {
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
        Class<?> k = L("org.w3c.dom.css.CSSStyleDeclaration");
        check(k.isInterface(), "org.w3c.dom.css.CSSStyleDeclaration is an interface");
        check(k.getInterfaces().length == 0, "CSSStyleDeclaration declares no superinterface");
        check(k.getMethod("getCssText") != null, "getCssText()");
        check(k.getMethod("setCssText", String.class) != null, "setCssText(String)");
        check(k.getMethod("getPropertyValue", String.class) != null, "getPropertyValue(String)");
        check(k.getMethod("getPropertyCSSValue", String.class) != null, "getPropertyCSSValue(String)");
        check(k.getMethod("removeProperty", String.class) != null, "removeProperty(String)");
        check(k.getMethod("getPropertyPriority", String.class) != null, "getPropertyPriority(String)");
        check(k.getMethod("setProperty", String.class, String.class, String.class) != null, "setProperty(String, String, String)");
        check(k.getMethod("getLength") != null, "getLength()");
        check(k.getMethod("item", int.class) != null, "item(int)");
        check(k.getMethod("getParentRule") != null, "getParentRule()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
