/*
 * @test
 * @summary javax.accessibility.AccessibleTable conformance: interface reflection.
 * @run main AccessibleTableTest
 */

public class AccessibleTableTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleTable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleCaption"), "method getAccessibleCaption");
        check(hasMethod(c, "setAccessibleCaption"), "method setAccessibleCaption");
        check(hasMethod(c, "getAccessibleSummary"), "method getAccessibleSummary");
        check(hasMethod(c, "setAccessibleSummary"), "method setAccessibleSummary");
        check(hasMethod(c, "getAccessibleRowCount"), "method getAccessibleRowCount");
        check(hasMethod(c, "getAccessibleColumnCount"), "method getAccessibleColumnCount");
        check(hasMethod(c, "getAccessibleAt"), "method getAccessibleAt");
        check(hasMethod(c, "getAccessibleRowExtentAt"), "method getAccessibleRowExtentAt");
        check(hasMethod(c, "getAccessibleColumnExtentAt"), "method getAccessibleColumnExtentAt");
        check(hasMethod(c, "getAccessibleRowHeader"), "method getAccessibleRowHeader");
        check(hasMethod(c, "setAccessibleRowHeader"), "method setAccessibleRowHeader");
        check(hasMethod(c, "getAccessibleColumnHeader"), "method getAccessibleColumnHeader");
        check(hasMethod(c, "setAccessibleColumnHeader"), "method setAccessibleColumnHeader");
        check(hasMethod(c, "getAccessibleRowDescription"), "method getAccessibleRowDescription");
        check(hasMethod(c, "setAccessibleRowDescription"), "method setAccessibleRowDescription");
        check(hasMethod(c, "getAccessibleColumnDescription"), "method getAccessibleColumnDescription");
        check(hasMethod(c, "setAccessibleColumnDescription"), "method setAccessibleColumnDescription");
        check(hasMethod(c, "isAccessibleSelected"), "method isAccessibleSelected");
        check(hasMethod(c, "isAccessibleRowSelected"), "method isAccessibleRowSelected");
        check(hasMethod(c, "isAccessibleColumnSelected"), "method isAccessibleColumnSelected");
        check(hasMethod(c, "getSelectedAccessibleRows"), "method getSelectedAccessibleRows");
        check(hasMethod(c, "getSelectedAccessibleColumns"), "method getSelectedAccessibleColumns");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + ModelChange constants ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleTable t = new RecTable();
        check(t.getAccessibleRowCount() == 2 && t.getAccessibleColumnCount() == 3, "dims");
        check(t.getAccessibleRowDescription(0) != null, "row description");
        check(t.getAccessibleColumnDescription(0) != null, "column description");
        check(t.getAccessibleRowHeader() != null, "row header");
        check(t.getAccessibleColumnHeader() != null, "column header");
        check(t.getAccessibleCaption() != null, "caption Accessible-typed");
        check(t.getAccessibleSummary() != null, "summary Accessible-typed");
        check(t.getSelectedAccessibleRows().length == 0, "selected rows");
        check(t.getSelectedAccessibleColumns().length == 0, "selected columns");
        check(t.isAccessibleSelected(0, 0) == false, "not selected");
        check(javax.accessibility.AccessibleTable.class.isInterface(), "interface");
        check(javax.accessibility.AccessibleExtendedTable.class.isInterface(), "extended interface");
        check(javax.accessibility.AccessibleTable.class.isAssignableFrom(javax.accessibility.AccessibleExtendedTable.class), "extended extends base");
        check(javax.accessibility.AccessibleTableModelChange.INSERT == 1, "INSERT 1");
        check(javax.accessibility.AccessibleTableModelChange.UPDATE == 0, "UPDATE 0");
        check(javax.accessibility.AccessibleTableModelChange.DELETE == -1, "DELETE -1");
        javax.swing.JTable jt = new javax.swing.JTable(new Object[][] { { "a" }, { "b" } }, new Object[] { "h" });
        javax.accessibility.AccessibleTable real = jt.getAccessibleContext().getAccessibleTable();
        check(real != null, "JTable exposes table");
        check(real.getAccessibleRowCount() == 2 && real.getAccessibleColumnCount() == 1, "JTable dims");
    }
    static class RecTable implements javax.accessibility.AccessibleTable {
        public int getAccessibleRowCount() { return 2; }
        public int getAccessibleColumnCount() { return 3; }
        public javax.accessibility.Accessible getAccessibleAt(int r, int c) { return null; }
        public int getAccessibleRowExtentAt(int r, int c) { return 1; }
        public int getAccessibleColumnExtentAt(int r, int c) { return 1; }
        public javax.accessibility.AccessibleTable getAccessibleRowHeader() { return this; }
        public void setAccessibleRowHeader(javax.accessibility.AccessibleTable t) {}
        public javax.accessibility.AccessibleTable getAccessibleColumnHeader() { return this; }
        public void setAccessibleColumnHeader(javax.accessibility.AccessibleTable t) {}
        public javax.accessibility.Accessible getAccessibleRowDescription(int r) { return new RecAcc(); }
        public void setAccessibleRowDescription(int r, javax.accessibility.Accessible a) {}
        public javax.accessibility.Accessible getAccessibleColumnDescription(int c) { return new RecAcc(); }
        public void setAccessibleColumnDescription(int c, javax.accessibility.Accessible a) {}
        public boolean isAccessibleSelected(int r, int c) { return false; }
        public boolean isAccessibleColumnSelected(int c) { return false; }
        public boolean isAccessibleRowSelected(int r) { return false; }
        public javax.accessibility.Accessible getAccessibleSelection() { return null; }
        public int[] getSelectedAccessibleRows() { return new int[0]; }
        public int[] getSelectedAccessibleColumns() { return new int[0]; }
        public javax.accessibility.Accessible getAccessibleCaption() { return new RecAcc(); }
        public void setAccessibleCaption(javax.accessibility.Accessible a) {}
        public javax.accessibility.Accessible getAccessibleSummary() { return new RecAcc(); }
        public void setAccessibleSummary(javax.accessibility.Accessible a) {}
    }
    static class RecAcc implements javax.accessibility.Accessible {
        public javax.accessibility.AccessibleContext getAccessibleContext() { return null; }
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
