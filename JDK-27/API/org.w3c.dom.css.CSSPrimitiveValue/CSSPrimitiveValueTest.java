/*
 * @test
 * @summary org.w3c.dom.css.CSSPrimitiveValue conformance: primitive value + unit constants.
 * @run main CSSPrimitiveValueTest
 */

public class CSSPrimitiveValueTest {
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
        Class<?> k = L("org.w3c.dom.css.CSSPrimitiveValue");
        check(k.isInterface(), "org.w3c.dom.css.CSSPrimitiveValue is an interface");
        check(L("org.w3c.dom.css.CSSValue").isAssignableFrom(k), "CSSPrimitiveValue extends CSSValue");
        check(k.getInterfaces().length >= 1, "CSSPrimitiveValue has superinterface");
        check(sconst("org.w3c.dom.css.CSSPrimitiveValue", "CSS_UNKNOWN") == 0, "CSS_UNKNOWN == 0");
        check(sconst("org.w3c.dom.css.CSSPrimitiveValue", "CSS_NUMBER") == 1, "CSS_NUMBER == 1");
        check(sconst("org.w3c.dom.css.CSSPrimitiveValue", "CSS_PERCENTAGE") == 2, "CSS_PERCENTAGE == 2");
        check(sconst("org.w3c.dom.css.CSSPrimitiveValue", "CSS_EMS") == 3, "CSS_EMS == 3");
        check(sconst("org.w3c.dom.css.CSSPrimitiveValue", "CSS_PX") == 5, "CSS_PX == 5");
        check(sconst("org.w3c.dom.css.CSSPrimitiveValue", "CSS_STRING") == 19, "CSS_STRING == 19");
        check(sconst("org.w3c.dom.css.CSSPrimitiveValue", "CSS_RECT") == 24, "CSS_RECT == 24");
        check(sconst("org.w3c.dom.css.CSSPrimitiveValue", "CSS_RGBCOLOR") == 25, "CSS_RGBCOLOR == 25");
        check(k.getMethod("getPrimitiveType") != null, "getPrimitiveType()");
        check(k.getMethod("setFloatValue", short.class, float.class) != null, "setFloatValue(short, float)");
        check(k.getMethod("getFloatValue", short.class) != null, "getFloatValue(short)");
        check(k.getMethod("setStringValue", short.class, String.class) != null, "setStringValue(short, String)");
        check(k.getMethod("getStringValue") != null, "getStringValue()");
        check(k.getMethod("getCounterValue") != null, "getCounterValue()");
        check(k.getMethod("getRectValue") != null, "getRectValue()");
        check(k.getMethod("getRGBColorValue") != null, "getRGBColorValue()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
