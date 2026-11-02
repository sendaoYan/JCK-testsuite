/*
 * @test
 * @summary java.awt.image.renderable.RenderContext conformance: class reflection.
 * @run main RenderContextTest
 */

public class RenderContextTest {
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
        Class<?> c = Class.forName("java.awt.image.renderable.RenderContext");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
        check(hasMethod(c, "setRenderingHints"), "method setRenderingHints");
        check(hasMethod(c, "setTransform"), "method setTransform");
        check(hasMethod(c, "preConcatenateTransform"), "method preConcatenateTransform");
        check(hasMethod(c, "preConcetenateTransform"), "method preConcetenateTransform");
        check(hasMethod(c, "concatenateTransform"), "method concatenateTransform");
        check(hasMethod(c, "concetenateTransform"), "method concetenateTransform");
        check(hasMethod(c, "getTransform"), "method getTransform");
        check(hasMethod(c, "setAreaOfInterest"), "method setAreaOfInterest");
        check(hasMethod(c, "getAreaOfInterest"), "method getAreaOfInterest");
        check(hasMethod(c, "clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
