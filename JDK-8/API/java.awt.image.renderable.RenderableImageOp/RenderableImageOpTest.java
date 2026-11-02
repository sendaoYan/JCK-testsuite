/*
 * @test
 * @summary java.awt.image.renderable.RenderableImageOp conformance: class reflection.
 * @run main RenderableImageOpTest
 */

public class RenderableImageOpTest {
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
        Class<?> c = Class.forName("java.awt.image.renderable.RenderableImageOp");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.renderable.RenderableImage"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getSources"), "method getSources");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getPropertyNames"), "method getPropertyNames");
        check(hasMethod(c, "isDynamic"), "method isDynamic");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getMinX"), "method getMinX");
        check(hasMethod(c, "getMinY"), "method getMinY");
        check(hasMethod(c, "setParameterBlock"), "method setParameterBlock");
        check(hasMethod(c, "getParameterBlock"), "method getParameterBlock");
        check(hasMethod(c, "createScaledRendering"), "method createScaledRendering");
        check(hasMethod(c, "createDefaultRendering"), "method createDefaultRendering");
        check(hasMethod(c, "createRendering"), "method createRendering");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
