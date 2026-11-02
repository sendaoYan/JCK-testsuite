/*
 * @test
 * @summary java.awt.font.LineMetrics conformance: abstract class reflection.
 * @run main LineMetricsTest
 */

public class LineMetricsTest {
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
        Class<?> c = Class.forName("java.awt.font.LineMetrics");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getNumChars"), "method getNumChars");
        check(hasMethod(c, "getAscent"), "method getAscent");
        check(hasMethod(c, "getDescent"), "method getDescent");
        check(hasMethod(c, "getLeading"), "method getLeading");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getBaselineIndex"), "method getBaselineIndex");
        check(hasMethod(c, "getBaselineOffsets"), "method getBaselineOffsets");
        check(hasMethod(c, "getStrikethroughOffset"), "method getStrikethroughOffset");
        check(hasMethod(c, "getStrikethroughThickness"), "method getStrikethroughThickness");
        check(hasMethod(c, "getUnderlineOffset"), "method getUnderlineOffset");
        check(hasMethod(c, "getUnderlineThickness"), "method getUnderlineThickness");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
