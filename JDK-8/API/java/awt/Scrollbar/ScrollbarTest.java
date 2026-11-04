/*
 * @test
 * @summary java.awt.Scrollbar conformance: class reflection.
 * @run main ScrollbarTest
 */

public class ScrollbarTest {
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
        Class<?> c = Class.forName("java.awt.Scrollbar");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Adjustable", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getOrientation"), "method getOrientation");
        check(hasMethod(c, "setOrientation"), "method setOrientation");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "setValue"), "method setValue");
        check(hasMethod(c, "getMinimum"), "method getMinimum");
        check(hasMethod(c, "setMinimum"), "method setMinimum");
        check(hasMethod(c, "getMaximum"), "method getMaximum");
        check(hasMethod(c, "setMaximum"), "method setMaximum");
        check(hasMethod(c, "getVisibleAmount"), "method getVisibleAmount");
        check(hasMethod(c, "getVisible"), "method getVisible");
        check(hasMethod(c, "setVisibleAmount"), "method setVisibleAmount");
        check(hasMethod(c, "setUnitIncrement"), "method setUnitIncrement");
        check(hasMethod(c, "setLineIncrement"), "method setLineIncrement");
        check(hasMethod(c, "getUnitIncrement"), "method getUnitIncrement");
        check(hasMethod(c, "getLineIncrement"), "method getLineIncrement");
        check(hasMethod(c, "setBlockIncrement"), "method setBlockIncrement");
        check(hasMethod(c, "setPageIncrement"), "method setPageIncrement");
        check(hasMethod(c, "getBlockIncrement"), "method getBlockIncrement");
        check(hasMethod(c, "getPageIncrement"), "method getPageIncrement");
        check(hasMethod(c, "setValues"), "method setValues");
        check(hasMethod(c, "getValueIsAdjusting"), "method getValueIsAdjusting");
        check(hasMethod(c, "setValueIsAdjusting"), "method setValueIsAdjusting");
        check(hasMethod(c, "addAdjustmentListener"), "method addAdjustmentListener");
        check(hasMethod(c, "removeAdjustmentListener"), "method removeAdjustmentListener");
        check(hasMethod(c, "getAdjustmentListeners"), "method getAdjustmentListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
