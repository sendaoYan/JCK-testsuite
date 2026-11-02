/*
 * @test
 * @summary org.w3c.dom.css.Rect conformance: rectangle.
 * @run main RectTest
 */

public class RectTest {
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
        Class<?> k = L("org.w3c.dom.css.Rect");
        check(k.isInterface(), "org.w3c.dom.css.Rect is an interface");
        check(k.getInterfaces().length == 0, "Rect declares no superinterface");
        check(k.getMethod("getTop") != null, "getTop()");
        check(k.getMethod("getRight") != null, "getRight()");
        check(k.getMethod("getBottom") != null, "getBottom()");
        check(k.getMethod("getLeft") != null, "getLeft()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
