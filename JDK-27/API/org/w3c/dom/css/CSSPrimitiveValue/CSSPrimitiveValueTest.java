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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_UNKNOWN == 0, "CSS_UNKNOWN == 0");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_NUMBER == 1, "CSS_NUMBER == 1");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE == 2, "CSS_PERCENTAGE == 2");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_EMS == 3, "CSS_EMS == 3");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_EXS == 4, "CSS_EXS == 4");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_PX == 5, "CSS_PX == 5");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_CM == 6, "CSS_CM == 6");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_MM == 7, "CSS_MM == 7");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_IN == 8, "CSS_IN == 8");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_PT == 9, "CSS_PT == 9");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_PC == 10, "CSS_PC == 10");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_DEG == 11, "CSS_DEG == 11");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_RAD == 12, "CSS_RAD == 12");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_GRAD == 13, "CSS_GRAD == 13");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_MS == 14, "CSS_MS == 14");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_S == 15, "CSS_S == 15");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_HZ == 16, "CSS_HZ == 16");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_KHZ == 17, "CSS_KHZ == 17");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_DIMENSION == 18, "CSS_DIMENSION == 18");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_STRING == 19, "CSS_STRING == 19");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_URI == 20, "CSS_URI == 20");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT == 21, "CSS_IDENT == 21");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_ATTR == 22, "CSS_ATTR == 22");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_COUNTER == 23, "CSS_COUNTER == 23");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_RECT == 24, "CSS_RECT == 24");
        check(org.w3c.dom.css.CSSPrimitiveValue.CSS_RGBCOLOR == 25, "CSS_RGBCOLOR == 25");
        org.w3c.dom.css.CSSPrimitiveValue a = new org.w3c.dom.css.CSSPrimitiveValue(){
            public short getPrimitiveType() { return (short)2; }
            public void setFloatValue(short p0, float p1) { }
            public float getFloatValue(short p0) { return 5.5f; }
            public void setStringValue(short p0, java.lang.String p1) { }
            public java.lang.String getStringValue() { return "s"; }
            public org.w3c.dom.css.Counter getCounterValue() { return null; }
            public org.w3c.dom.css.Rect getRectValue() { return null; }
            public org.w3c.dom.css.RGBColor getRGBColorValue() { return null; }
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public short getCssValueType() { return (short)2; }
        };
        check(a != null, "anon impl of CSSPrimitiveValue");
        check(a.getPrimitiveType() == (short)2, "getPrimitiveType() returns fixed default");
        a.setFloatValue((short)2, 5.5f);
        check(a.getFloatValue((short)2) == 5.5f, "getFloatValue() returns fixed default");
        a.setStringValue((short)2, "s");
        check("s".equals(a.getStringValue()), "getStringValue() returns fixed default");
        check(a.getCounterValue() == null, "getCounterValue() returns fixed default");
        check(a.getRectValue() == null, "getRectValue() returns fixed default");
        check(a.getRGBColorValue() == null, "getRGBColorValue() returns fixed default");
        check("s".equals(a.getCssText()), "getCssText() returns fixed default");
        a.setCssText("s");
        check(a.getCssValueType() == (short)2, "getCssValueType() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
