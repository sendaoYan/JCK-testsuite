/*
 * @test
 * @summary java.awt.image.ConvolveOp conformance: class reflection.
 * @run main ConvolveOpTest
 */

public class ConvolveOpTest {
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
        Class<?> c = Class.forName("java.awt.image.ConvolveOp");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.BufferedImageOp", "java.awt.image.RasterOp"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getEdgeCondition"), "method getEdgeCondition");
        check(hasMethod(c, "getKernel"), "method getKernel");
        check(hasMethod(c, "filter"), "method filter");
        check(hasMethod(c, "createCompatibleDestImage"), "method createCompatibleDestImage");
        check(hasMethod(c, "createCompatibleDestRaster"), "method createCompatibleDestRaster");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "getPoint2D"), "method getPoint2D");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
