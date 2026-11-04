/*
 * @test
 * @summary javax.accessibility.AccessibleSelection conformance: white-box structure + black-box behavior.
 * @run main AccessibleSelectionTest
 */

import java.lang.reflect.*;
import java.util.BitSet;

public class AccessibleSelectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleSelection");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleSelectionCount"), "declares getAccessibleSelectionCount");
        check(hasMethod(c, "getAccessibleSelection"), "declares getAccessibleSelection");
        check(hasMethod(c, "isAccessibleChildSelected"), "declares isAccessibleChildSelected");
        check(hasMethod(c, "addAccessibleSelection"), "declares addAccessibleSelection");
        check(hasMethod(c, "removeAccessibleSelection"), "declares removeAccessibleSelection");
        check(hasMethod(c, "clearAccessibleSelection"), "declares clearAccessibleSelection");
        check(hasMethod(c, "selectAllAccessibleSelection"), "declares selectAllAccessibleSelection");
    }

    static void testBlackBox() throws Exception {
        final int childCount = 3;
        final BitSet selected = new BitSet(childCount);
        javax.accessibility.AccessibleSelection sel = new javax.accessibility.AccessibleSelection() {
            public int getAccessibleSelectionCount() { return selected.cardinality(); }
            public javax.accessibility.Accessible getAccessibleSelection(int i) {
                if (!selected.get(i)) return null;
                final int idx = i;
                return new javax.accessibility.Accessible() {
                    public javax.accessibility.AccessibleContext getAccessibleContext() { return null; }
                };
            }
            public boolean isAccessibleChildSelected(int i) { return selected.get(i); }
            public void addAccessibleSelection(int i) { selected.set(i); }
            public void removeAccessibleSelection(int i) { selected.clear(i); }
            public void clearAccessibleSelection() { selected.clear(); }
            public void selectAllAccessibleSelection() { selected.set(0, childCount); }
        };

        check(sel.getAccessibleSelectionCount() == 0, "initial selection count 0");
        check(!sel.isAccessibleChildSelected(0), "initial child not selected");

        sel.addAccessibleSelection(0);
        sel.addAccessibleSelection(2);
        check(sel.getAccessibleSelectionCount() == 2, "count 2 after two adds");
        check(sel.isAccessibleChildSelected(0), "child 0 selected");
        check(sel.isAccessibleChildSelected(2), "child 2 selected");
        check(!sel.isAccessibleChildSelected(1), "child 1 not selected");
        check(sel.getAccessibleSelection(0) != null, "getAccessibleSelection(0) non-null");

        sel.removeAccessibleSelection(0);
        check(sel.getAccessibleSelectionCount() == 1, "count 1 after remove");
        check(!sel.isAccessibleChildSelected(0), "child 0 unselected");

        sel.selectAllAccessibleSelection();
        check(sel.getAccessibleSelectionCount() == childCount, "selectAll selects all");
        sel.clearAccessibleSelection();
        check(sel.getAccessibleSelectionCount() == 0, "clearAccessibleSelection empties");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
