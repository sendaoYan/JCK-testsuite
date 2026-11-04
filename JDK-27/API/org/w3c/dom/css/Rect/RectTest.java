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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.css.Rect");
        check(k.isInterface(), "org.w3c.dom.css.Rect is an interface");
        check(k.getInterfaces().length == 0, "Rect declares no superinterface");
        check(k.getMethod("getTop") != null, "getTop()");
        check(k.getMethod("getRight") != null, "getRight()");
        check(k.getMethod("getBottom") != null, "getBottom()");
        check(k.getMethod("getLeft") != null, "getLeft()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.Rect a = new org.w3c.dom.css.Rect(){
            public org.w3c.dom.css.CSSPrimitiveValue getTop() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getRight() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getBottom() { return null; }
            public org.w3c.dom.css.CSSPrimitiveValue getLeft() { return null; }
        };
        check(a != null, "anon impl of Rect");
        check(a.getTop() == null, "getTop() returns fixed default");
        check(a.getRight() == null, "getRight() returns fixed default");
        check(a.getBottom() == null, "getBottom() returns fixed default");
        check(a.getLeft() == null, "getLeft() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
