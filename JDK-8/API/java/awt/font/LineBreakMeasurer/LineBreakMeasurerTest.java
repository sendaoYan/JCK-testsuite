/*
 * @test
 * @summary java.awt.font.LineBreakMeasurer conformance: class reflection.
 * @run main LineBreakMeasurerTest
 */

public class LineBreakMeasurerTest {
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
        Class<?> c = Class.forName("java.awt.font.LineBreakMeasurer");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "nextOffset"), "method nextOffset");
        check(hasMethod(c, "nextLayout"), "method nextLayout");
        check(hasMethod(c, "getPosition"), "method getPosition");
        check(hasMethod(c, "setPosition"), "method setPosition");
        check(hasMethod(c, "insertChar"), "method insertChar");
        check(hasMethod(c, "deleteChar"), "method deleteChar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
