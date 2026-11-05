/*
 * @test
 * @summary javax.accessibility.AccessibleSelection conformance: interface reflection.
 * @run main AccessibleSelectionTest
 */

public class AccessibleSelectionTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleSelection");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleSelectionCount"), "method getAccessibleSelectionCount");
        check(hasMethod(c, "getAccessibleSelection"), "method getAccessibleSelection");
        check(hasMethod(c, "isAccessibleChildSelected"), "method isAccessibleChildSelected");
        check(hasMethod(c, "addAccessibleSelection"), "method addAccessibleSelection");
        check(hasMethod(c, "removeAccessibleSelection"), "method removeAccessibleSelection");
        check(hasMethod(c, "clearAccessibleSelection"), "method clearAccessibleSelection");
        check(hasMethod(c, "selectAllAccessibleSelection"), "method selectAllAccessibleSelection");
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
