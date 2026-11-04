/*
 * @test
 * @summary java.awt.ScrollPaneAdjustable conformance: class reflection.
 * @run main ScrollPaneAdjustableTest
 */

public class ScrollPaneAdjustableTest {
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
        Class<?> c = Class.forName("java.awt.ScrollPaneAdjustable");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Adjustable", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
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
        check(hasMethod(c, "setValueIsAdjusting"), "method setValueIsAdjusting");
        check(hasMethod(c, "getValueIsAdjusting"), "method getValueIsAdjusting");
        check(hasMethod(c, "setValue"), "method setValue");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "addAdjustmentListener"), "method addAdjustmentListener");
        check(hasMethod(c, "removeAdjustmentListener"), "method removeAdjustmentListener");
        check(hasMethod(c, "getAdjustmentListeners"), "method getAdjustmentListeners");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
