/*
 * @test
 * @summary org.w3c.dom.css.CSS2Properties conformance: CSS2 property accessors.
 * @run main CSS2PropertiesTest
 */

public class CSS2PropertiesTest {
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
        Class<?> k = L("org.w3c.dom.css.CSS2Properties");
        check(k.isInterface(), "org.w3c.dom.css.CSS2Properties is an interface");
        check(k.getInterfaces().length == 0, "CSS2Properties declares no superinterface");
        check(k.getMethod("getAzimuth") != null, "getAzimuth()");
        check(k.getMethod("getBackground") != null, "getBackground()");
        check(k.getMethod("getColor") != null, "getColor()");
        check(k.getMethod("getCssFloat") != null, "getCssFloat()");
        check(k.getMethod("getDisplay") != null, "getDisplay()");
        check(k.getMethod("getZIndex") != null, "getZIndex()");
        check(k.getMethod("setZIndex", String.class) != null, "setZIndex(String)");
        check(k.getMethod("setBackground", String.class) != null, "setBackground(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
