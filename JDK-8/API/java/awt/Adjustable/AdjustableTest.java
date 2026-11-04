/*
 * @test
 * @summary java.awt.Adjustable conformance: interface reflection.
 * @run main AdjustableTest
 */

public class AdjustableTest {
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
        Class<?> c = Class.forName("java.awt.Adjustable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getOrientation"), "method getOrientation");
        check(hasMethod(c, "setMinimum"), "method setMinimum");
        check(hasMethod(c, "getMinimum"), "method getMinimum");
        check(hasMethod(c, "setMaximum"), "method setMaximum");
        check(hasMethod(c, "getMaximum"), "method getMaximum");
        check(hasMethod(c, "setUnitIncrement"), "method setUnitIncrement");
        check(hasMethod(c, "getUnitIncrement"), "method getUnitIncrement");
        check(hasMethod(c, "setBlockIncrement"), "method setBlockIncrement");
        check(hasMethod(c, "getBlockIncrement"), "method getBlockIncrement");
        check(hasMethod(c, "setVisibleAmount"), "method setVisibleAmount");
        check(hasMethod(c, "getVisibleAmount"), "method getVisibleAmount");
        check(hasMethod(c, "setValue"), "method setValue");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "addAdjustmentListener"), "method addAdjustmentListener");
        check(hasMethod(c, "removeAdjustmentListener"), "method removeAdjustmentListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
