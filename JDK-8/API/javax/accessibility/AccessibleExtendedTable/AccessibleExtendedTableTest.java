/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedTable conformance: interface reflection.
 * @run main AccessibleExtendedTableTest
 */

public class AccessibleExtendedTableTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedTable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleTable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getAccessibleRow"), "method getAccessibleRow");
        check(hasMethod(c, "getAccessibleColumn"), "method getAccessibleColumn");
        check(hasMethod(c, "getAccessibleIndex"), "method getAccessibleIndex");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + JTable active cell ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleExtendedTable et = new javax.accessibility.AccessibleExtendedTable() {
            final javax.accessibility.AccessibleTable base;
            { base = null; }
            public int getAccessibleRow(int index) { return index / 3; }
            public int getAccessibleColumn(int index) { return index % 3; }
            public int getAccessibleIndex(int r, int c) { return r * 3 + c; }
            public int getAccessibleRowCount() { return 2; }
            public int getAccessibleColumnCount() { return 3; }
            public javax.accessibility.Accessible getAccessibleAt(int r, int c) { return null; }
            public int getAccessibleRowExtentAt(int r, int c) { return 1; }
            public int getAccessibleColumnExtentAt(int r, int c) { return 1; }
            public javax.accessibility.AccessibleTable getAccessibleRowHeader() { return null; }
            public void setAccessibleRowHeader(javax.accessibility.AccessibleTable t) {}
            public javax.accessibility.AccessibleTable getAccessibleColumnHeader() { return null; }
            public void setAccessibleColumnHeader(javax.accessibility.AccessibleTable t) {}
            public javax.accessibility.Accessible getAccessibleRowDescription(int r) { return null; }
            public void setAccessibleRowDescription(int r, javax.accessibility.Accessible a) {}
            public javax.accessibility.Accessible getAccessibleColumnDescription(int c) { return null; }
            public void setAccessibleColumnDescription(int c, javax.accessibility.Accessible a) {}
            public boolean isAccessibleSelected(int r, int c) { return false; }
            public boolean isAccessibleColumnSelected(int c) { return false; }
            public boolean isAccessibleRowSelected(int r) { return false; }
            public javax.accessibility.Accessible getAccessibleSelection() { return null; }
            public int[] getSelectedAccessibleRows() { return new int[0]; }
            public int[] getSelectedAccessibleColumns() { return new int[0]; }
            public javax.accessibility.Accessible getAccessibleCaption() { return null; }
            public void setAccessibleCaption(javax.accessibility.Accessible a) {}
            public javax.accessibility.Accessible getAccessibleSummary() { return null; }
            public void setAccessibleSummary(javax.accessibility.Accessible a) {}
        };
        check(et.getAccessibleRow(5) == 1 && et.getAccessibleColumn(5) == 2, "linear index -> r/c");
        check(et.getAccessibleIndex(1, 2) == 5, "r/c -> linear index");
        check(javax.accessibility.AccessibleExtendedTable.class.isInterface(), "interface");
        javax.swing.JTable jt = new javax.swing.JTable(new Object[][] { { "a", "b" }, { "c", "d" } }, new Object[] { "h1", "h2" });
        javax.accessibility.AccessibleTable at = jt.getAccessibleContext().getAccessibleTable();
        check(at instanceof javax.accessibility.AccessibleExtendedTable, "JTable table is extended");
        javax.accessibility.AccessibleExtendedTable jet = (javax.accessibility.AccessibleExtendedTable) at;
        check(jet.getAccessibleIndex(1, 1) == 3, "JTable index 3 for (1,1)");
        check(jet.getAccessibleRow(2) == 1 && jet.getAccessibleColumn(2) == 0, "JTable r/c of 2");
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
