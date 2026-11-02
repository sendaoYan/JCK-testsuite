/*
 * @test
 * @summary java.awt.image.PixelGrabber conformance: class reflection.
 * @run main PixelGrabberTest
 */

public class PixelGrabberTest {
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
        Class<?> c = Class.forName("java.awt.image.PixelGrabber");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.ImageConsumer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "startGrabbing"), "method startGrabbing");
        check(hasMethod(c, "abortGrabbing"), "method abortGrabbing");
        check(hasMethod(c, "grabPixels"), "method grabPixels");
        check(hasMethod(c, "getStatus"), "method getStatus");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setHints"), "method setHints");
        check(hasMethod(c, "setProperties"), "method setProperties");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "imageComplete"), "method imageComplete");
        check(hasMethod(c, "status"), "method status");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
