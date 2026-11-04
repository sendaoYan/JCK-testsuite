/*
 * @test
 * @summary java.awt.image.renderable.ContextualRenderedImageFactory conformance: interface reflection.
 * @run main ContextualRenderedImageFactoryTest
 */

public class ContextualRenderedImageFactoryTest {
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
        Class<?> c = Class.forName("java.awt.image.renderable.ContextualRenderedImageFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.renderable.RenderedImageFactory"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "mapRenderContext"), "method mapRenderContext");
        check(hasMethod(c, "create"), "method create");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getPropertyNames"), "method getPropertyNames");
        check(hasMethod(c, "isDynamic"), "method isDynamic");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
