/*
 * @test
 * @summary java.awt.image.BufferedImageFilter conformance: class reflection.
 * @run main BufferedImageFilterTest
 */

public class BufferedImageFilterTest {
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
        Class<?> c = Class.forName("java.awt.image.BufferedImageFilter");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ImageFilter"), "superclass java.awt.image.ImageFilter");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getBufferedImageOp"), "method getBufferedImageOp");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "imageComplete"), "method imageComplete");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
