/*
 * @test
 * @summary java.awt.PrintJob conformance: abstract class reflection.
 * @run main PrintJobTest
 */

public class PrintJobTest {
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
        Class<?> c = Class.forName("java.awt.PrintJob");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getGraphics"), "method getGraphics");
        check(hasMethod(c, "getPageDimension"), "method getPageDimension");
        check(hasMethod(c, "getPageResolution"), "method getPageResolution");
        check(hasMethod(c, "lastPageFirst"), "method lastPageFirst");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "finalize"), "method finalize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
