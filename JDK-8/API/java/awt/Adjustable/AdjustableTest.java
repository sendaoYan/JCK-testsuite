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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        check(java.awt.Adjustable.class.isInterface(), "Adjustable is an interface");
        check(java.awt.Adjustable.HORIZONTAL == 0, "HORIZONTAL == 0");
        check(java.awt.Adjustable.VERTICAL == 1, "VERTICAL == 1");
        check(java.awt.Adjustable.NO_ORIENTATION == 2, "NO_ORIENTATION == 2");
        // minimal implementation honors set/get value contract
        class Adj implements java.awt.Adjustable {
            int orientation = java.awt.Adjustable.HORIZONTAL, value, min = 0, max = 100,
                unit = 1, block = 10, visible = 10;
            java.util.List<java.awt.event.AdjustmentListener> ls = new java.util.ArrayList<java.awt.event.AdjustmentListener>();
            public int getOrientation() { return orientation; }
            public void setMinimum(int m) { min = m; }
            public int getMinimum() { return min; }
            public void setMaximum(int m) { max = m; }
            public int getMaximum() { return max; }
            public void setUnitIncrement(int u) { unit = u; }
            public int getUnitIncrement() { return unit; }
            public void setBlockIncrement(int b) { block = b; }
            public int getBlockIncrement() { return block; }
            public void setVisibleAmount(int v) { visible = v; }
            public int getVisibleAmount() { return visible; }
            public void setValue(int v) { value = v; }
            public int getValue() { return value; }
            public void addAdjustmentListener(java.awt.event.AdjustmentListener l) { ls.add(l); }
            public void removeAdjustmentListener(java.awt.event.AdjustmentListener l) { ls.remove(l); }
        }
        Adj a = new Adj();
        a.setValue(42);
        check(a.getValue() == 42, "setValue/getValue round-trip");
        a.setMinimum(-5); a.setMaximum(200);
        check(a.getMinimum() == -5 && a.getMaximum() == 200, "min/max round-trip");
        // Adjustable constants usable by implementations (Scrollbar is headful; ScrollPaneAdjustable needs ScrollPane)
        check(java.awt.Adjustable.HORIZONTAL != java.awt.Adjustable.VERTICAL, "orientations distinct");
        check(java.awt.Adjustable.VERTICAL != java.awt.Adjustable.NO_ORIENTATION, "vertical distinct from none");
    }

}
