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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + real component without selection ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleSelection sel = new javax.accessibility.AccessibleSelection() {
            public int getAccessibleSelectionCount() { return 1; }
            public javax.accessibility.Accessible getAccessibleSelection(int i) { return null; }
            public boolean isAccessibleChildSelected(int i) { return i == 0; }
            public void addAccessibleSelection(int i) {}
            public void removeAccessibleSelection(int i) {}
            public void clearAccessibleSelection() {}
            public void selectAllAccessibleSelection() {}
        };
        check(sel.getAccessibleSelectionCount() == 1, "count");
        check(sel.isAccessibleChildSelected(0), "child 0 selected");
        check(!sel.isAccessibleChildSelected(1), "child 1 not");
        check(javax.accessibility.AccessibleSelection.class.isInterface(), "interface");
        javax.swing.JLabel l = new javax.swing.JLabel();
        check(l.getAccessibleContext().getAccessibleSelection() == null, "JLabel has no selection");
        javax.swing.JList<String> list = new javax.swing.JList<String>(new String[] { "a", "b" });
        javax.accessibility.AccessibleSelection ls = list.getAccessibleContext().getAccessibleSelection();
        check(ls != null, "JList exposes selection");
        check(ls.getAccessibleSelectionCount() == 0, "nothing selected initially");
        list.setSelectedIndex(1);
        check(ls.getAccessibleSelectionCount() == 1, "one after setSelectedIndex");
        check(ls.isAccessibleChildSelected(1), "index 1 selected");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // concrete AccessibleContext recording subclass
    static class RecCtx extends javax.accessibility.AccessibleContext {
        public String getAccessibleName() { return accessibleName; }
        public String getAccessibleDescription() { return accessibleDescription; }
        public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.UNKNOWN; }
        public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return new javax.accessibility.AccessibleStateSet(); }
        public int getAccessibleIndexInParent() { return -1; }
        public int getAccessibleChildrenCount() { return 0; }
        public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
        public java.util.Locale getLocale() throws java.awt.IllegalComponentStateException { return java.util.Locale.ENGLISH; }
    }

}
