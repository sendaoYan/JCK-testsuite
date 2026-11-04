/*
 * @test
 * @summary java.awt.font.ImageGraphicAttribute conformance: class reflection.
 * @run main ImageGraphicAttributeTest
 */

public class ImageGraphicAttributeTest {
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
        Class<?> c = Class.forName("java.awt.font.ImageGraphicAttribute");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.font.GraphicAttribute"), "superclass java.awt.font.GraphicAttribute");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAscent"), "method getAscent");
        check(hasMethod(c, "getDescent"), "method getDescent");
        check(hasMethod(c, "getAdvance"), "method getAdvance");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "draw"), "method draw");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
