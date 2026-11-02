/*
 * @test
 * @summary java.awt.image.ReplicateScaleFilter conformance: class reflection.
 * @run main ReplicateScaleFilterTest
 */

public class ReplicateScaleFilterTest {
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
        Class<?> c = Class.forName("java.awt.image.ReplicateScaleFilter");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ImageFilter"), "superclass java.awt.image.ImageFilter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setProperties"), "method setProperties");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setPixels"), "method setPixels");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
