/*
 * @test
 * @summary java.awt.image.VolatileImage conformance: abstract class reflection.
 * @run main VolatileImageTest
 */

public class VolatileImageTest {
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
        Class<?> c = Class.forName("java.awt.image.VolatileImage");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.Image"), "superclass java.awt.Image");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Transparency"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getSnapshot"), "method getSnapshot");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getSource"), "method getSource");
        check(hasMethod(c, "getGraphics"), "method getGraphics");
        check(hasMethod(c, "createGraphics"), "method createGraphics");
        check(hasMethod(c, "validate"), "method validate");
        check(hasMethod(c, "contentsLost"), "method contentsLost");
        check(hasMethod(c, "getCapabilities"), "method getCapabilities");
        check(hasMethod(c, "getTransparency"), "method getTransparency");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
