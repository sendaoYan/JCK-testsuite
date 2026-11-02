/*
 * @test
 * @summary org.w3c.dom.css.CSSRule conformance: rule type constants.
 * @run main CSSRuleTest
 */

public class CSSRuleTest {
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
        Class<?> k = L("org.w3c.dom.css.CSSRule");
        check(k.isInterface(), "org.w3c.dom.css.CSSRule is an interface");
        check(k.getInterfaces().length == 0, "CSSRule declares no superinterface");
        check(sconst("org.w3c.dom.css.CSSRule", "UNKNOWN_RULE") == 0, "UNKNOWN_RULE == 0");
        check(sconst("org.w3c.dom.css.CSSRule", "STYLE_RULE") == 1, "STYLE_RULE == 1");
        check(sconst("org.w3c.dom.css.CSSRule", "CHARSET_RULE") == 2, "CHARSET_RULE == 2");
        check(sconst("org.w3c.dom.css.CSSRule", "IMPORT_RULE") == 3, "IMPORT_RULE == 3");
        check(sconst("org.w3c.dom.css.CSSRule", "MEDIA_RULE") == 4, "MEDIA_RULE == 4");
        check(sconst("org.w3c.dom.css.CSSRule", "FONT_FACE_RULE") == 5, "FONT_FACE_RULE == 5");
        check(sconst("org.w3c.dom.css.CSSRule", "PAGE_RULE") == 6, "PAGE_RULE == 6");
        check(k.getMethod("getType") != null, "getType()");
        check(k.getMethod("getCssText") != null, "getCssText()");
        check(k.getMethod("setCssText", String.class) != null, "setCssText(String)");
        check(k.getMethod("getParentStyleSheet") != null, "getParentStyleSheet()");
        check(k.getMethod("getParentRule") != null, "getParentRule()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
