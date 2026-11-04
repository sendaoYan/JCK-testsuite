/*
 * @test
 * @summary java.awt.image.ImageFilter conformance: class reflection.
 * @run main ImageFilterTest
 */

public class ImageFilterTest {
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
        Class<?> c = Class.forName("java.awt.image.ImageFilter");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.ImageConsumer", "java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getFilterInstance"), "method getFilterInstance");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setProperties"), "method setProperties");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "setHints"), "method setHints");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "imageComplete"), "method imageComplete");
        check(hasMethod(c, "resendTopDownLeftRight"), "method resendTopDownLeftRight");
        check(hasMethod(c, "clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
