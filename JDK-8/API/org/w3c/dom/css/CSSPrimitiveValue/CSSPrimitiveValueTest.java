/*
 * @test
 * @summary org.w3c.dom.css.CSSPrimitiveValue conformance: reflection shape checks.
 * @run main CSSPrimitiveValueTest
 */

import java.lang.reflect.*;

public class CSSPrimitiveValueTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.css.CSSPrimitiveValue");
        check(c != null, "org.w3c.dom.css.CSSPrimitiveValue loads");
        check(c.getName().equals("org.w3c.dom.css.CSSPrimitiveValue"), "binary name is org.w3c.dom.css.CSSPrimitiveValue");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSPrimitiveValue is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSPrimitiveValue is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.css.CSSValue")), "direct superinterfaces");
        check(declares(c, "getPrimitiveType"), "declares getPrimitiveType");
        check(declares(c, "setFloatValue"), "declares setFloatValue");
        check(declares(c, "getFloatValue"), "declares getFloatValue");
        check(declares(c, "setStringValue"), "declares setStringValue");
        check(declares(c, "getStringValue"), "declares getStringValue");
        check(declares(c, "getCounterValue"), "declares getCounterValue");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSPrimitiveValue contract (26 unit constants + typed value channels) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        short[] units = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25};
        String[] names = {"CSS_UNKNOWN","CSS_NUMBER","CSS_PERCENTAGE","CSS_EMS","CSS_EXS","CSS_PX","CSS_CM","CSS_MM","CSS_IN","CSS_PT","CSS_PC","CSS_DEG","CSS_RAD","CSS_GRAD","CSS_MS","CSS_S","CSS_HZ","CSS_KHZ","CSS_DIMENSION","CSS_STRING","CSS_URI","CSS_IDENT","CSS_ATTR","CSS_COUNTER","CSS_RECT","CSS_RGBCOLOR"};
        for (int i = 0; i < names.length; i++)
            check(org.w3c.dom.css.CSSPrimitiveValue.class.getField(names[i]).getShort(null) == units[i], names[i] + " = " + units[i]);
        final float[] val = new float[]{72f};
        org.w3c.dom.css.CSSPrimitiveValue pv = new org.w3c.dom.css.CSSPrimitiveValue() {
            public short getPrimitiveType() { return org.w3c.dom.css.CSSPrimitiveValue.CSS_PT; }
            public void setFloatValue(short unit, float f) { val[0] = f; }
            public float getFloatValue(short unit) { return val[0]; }
            public void setStringValue(short type, String s) { }
            public String getStringValue() { return "pt"; }
            public org.w3c.dom.css.Counter getCounterValue() { return null; }
            public org.w3c.dom.css.Rect getRectValue() { return null; }
            public org.w3c.dom.css.RGBColor getRGBColorValue() { return null; }
            public String getCssText() { return null; }
            public void setCssText(String t) { }
            public short getCssValueType() { return org.w3c.dom.css.CSSValue.CSS_PRIMITIVE_VALUE; }
        };
        check(pv.getPrimitiveType() == org.w3c.dom.css.CSSPrimitiveValue.CSS_PT, "primitive type reports CSS_PT");
        pv.setFloatValue(org.w3c.dom.css.CSSPrimitiveValue.CSS_PT, 96f);
        check(pv.getFloatValue(org.w3c.dom.css.CSSPrimitiveValue.CSS_PT) == 96f, "float value round-trip");
        check(pv.getCssValueType() == org.w3c.dom.css.CSSValue.CSS_PRIMITIVE_VALUE, "cssValueType reports CSS_PRIMITIVE_VALUE(1)");
    }
}
