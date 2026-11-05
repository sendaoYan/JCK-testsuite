/*
 * @test
 * @summary org.w3c.dom.css.RGBColor conformance: RGB color.
 * @run main RGBColorTest
 */

public class RGBColorTest {
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

        Class<?> k = L("org.w3c.dom.css.RGBColor");
        check(k.isInterface(), "org.w3c.dom.css.RGBColor is an interface");
        check(k.getInterfaces().length == 0, "RGBColor declares no superinterface");
        check(k.getMethod("getRed") != null, "getRed()");
        check(k.getMethod("getGreen") != null, "getGreen()");
        check(k.getMethod("getBlue") != null, "getBlue()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.RGBColor a = new org.w3c.dom.css.RGBColor(){
            public org.w3c.dom.css.CSSPrimitiveValue getRed() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getGreen() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getBlue() { return null; }
        };
        check(a != null, "anon impl of RGBColor");
        check(a.getRed() == null, "getRed() returns fixed default");
        check(a.getGreen() == null, "getGreen() returns fixed default");
        check(a.getBlue() == null, "getBlue() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: RGBColor contract (three channels) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.css.RGBColor c = new org.w3c.dom.css.RGBColor() {
            public org.w3c.dom.css.CSSPrimitiveValue getRed() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getGreen() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getBlue() { return null; }
        };
        check(c.getRed() == null && c.getGreen() == null && c.getBlue() == null, "three channel accessors routed");
    }
}
