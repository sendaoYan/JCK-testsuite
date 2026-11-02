/*
 * @test
 * @summary java.awt.font.GlyphMetrics conformance: class reflection.
 * @run main GlyphMetricsTest
 */

public class GlyphMetricsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.font.GlyphMetrics");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAdvance"), "method getAdvance");
        check(hasMethod(c, "getAdvanceX"), "method getAdvanceX");
        check(hasMethod(c, "getAdvanceY"), "method getAdvanceY");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "getLSB"), "method getLSB");
        check(hasMethod(c, "getRSB"), "method getRSB");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "isStandard"), "method isStandard");
        check(hasMethod(c, "isLigature"), "method isLigature");
        check(hasMethod(c, "isCombining"), "method isCombining");
        check(hasMethod(c, "isComponent"), "method isComponent");
        check(hasMethod(c, "isWhitespace"), "method isWhitespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
