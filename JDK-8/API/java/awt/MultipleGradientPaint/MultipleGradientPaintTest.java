/*
 * @test
 * @summary java.awt.MultipleGradientPaint conformance: abstract class reflection.
 * @run main MultipleGradientPaintTest
 */

public class MultipleGradientPaintTest {
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
        Class<?> c = Class.forName("java.awt.MultipleGradientPaint");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Paint"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getFractions"), "method getFractions");
        check(hasMethod(c, "getColors"), "method getColors");
        check(hasMethod(c, "getCycleMethod"), "method getCycleMethod");
        check(hasMethod(c, "getColorSpace"), "method getColorSpace");
        check(hasMethod(c, "getTransform"), "method getTransform");
        check(hasMethod(c, "getTransparency"), "method getTransparency");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
