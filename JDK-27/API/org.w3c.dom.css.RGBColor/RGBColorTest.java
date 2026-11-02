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

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.css.RGBColor");
        check(k.isInterface(), "org.w3c.dom.css.RGBColor is an interface");
        check(k.getInterfaces().length == 0, "RGBColor declares no superinterface");
        check(k.getMethod("getRed") != null, "getRed()");
        check(k.getMethod("getGreen") != null, "getGreen()");
        check(k.getMethod("getBlue") != null, "getBlue()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
