/*
 * @test
 * @summary javax.accessibility.AccessibleTable conformance: white-box structure + black-box behavior.
 * @run main AccessibleTableTest
 */

import java.lang.reflect.*;

public class AccessibleTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static javax.accessibility.Accessible makeAccessible(final String tag) {
        return new javax.accessibility.Accessible() {
            public javax.accessibility.AccessibleContext getAccessibleContext() { return null; }
        };
    }

    static class TableModel implements javax.accessibility.AccessibleTable {
        int rows = 3, cols = 4;
        javax.accessibility.Accessible caption, summary;
        javax.accessibility.AccessibleTable rowHeader, colHeader;
        javax.accessibility.Accessible[] rowDesc = new javax.accessibility.Accessible[3];
        javax.accessibility.Accessible[] colDesc = new javax.accessibility.Accessible[4];
        public javax.accessibility.Accessible getAccessibleCaption() { return caption; }
        public void setAccessibleCaption(javax.accessibility.Accessible a) { caption = a; }
        public javax.accessibility.Accessible getAccessibleSummary() { return summary; }
        public void setAccessibleSummary(javax.accessibility.Accessible a) { summary = a; }
        public int getAccessibleRowCount() { return rows; }
        public int getAccessibleColumnCount() { return cols; }
        public javax.accessibility.Accessible getAccessibleAt(int r, int c) { return makeAccessible(r + "," + c); }
        public int getAccessibleRowExtentAt(int r, int c) { return 1; }
        public int getAccessibleColumnExtentAt(int r, int c) { return 1; }
        public javax.accessibility.AccessibleTable getAccessibleRowHeader() { return rowHeader; }
        public void setAccessibleRowHeader(javax.accessibility.AccessibleTable t) { rowHeader = t; }
        public javax.accessibility.AccessibleTable getAccessibleColumnHeader() { return colHeader; }
        public void setAccessibleColumnHeader(javax.accessibility.AccessibleTable t) { colHeader = t; }
        public javax.accessibility.Accessible getAccessibleRowDescription(int r) { return rowDesc[r]; }
        public void setAccessibleRowDescription(int r, javax.accessibility.Accessible a) { rowDesc[r] = a; }
        public javax.accessibility.Accessible getAccessibleColumnDescription(int c) { return colDesc[c]; }
        public void setAccessibleColumnDescription(int c, javax.accessibility.Accessible a) { colDesc[c] = a; }
        public boolean isAccessibleSelected(int r, int c) { return r == 0 && c == 0; }
        public boolean isAccessibleRowSelected(int r) { return r == 0; }
        public boolean isAccessibleColumnSelected(int c) { return c == 0; }
        public int[] getSelectedAccessibleRows() { return new int[]{0}; }
        public int[] getSelectedAccessibleColumns() { return new int[]{0}; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleTable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleCaption"), "declares getAccessibleCaption");
        check(hasMethod(c, "setAccessibleCaption"), "declares setAccessibleCaption");
        check(hasMethod(c, "getAccessibleSummary"), "declares getAccessibleSummary");
        check(hasMethod(c, "setAccessibleSummary"), "declares setAccessibleSummary");
        check(hasMethod(c, "getAccessibleRowCount"), "declares getAccessibleRowCount");
    }

    static void testBlackBox() throws Exception {
        TableModel t = new TableModel();
        check(t.getAccessibleRowCount() == 3, "row count");
        check(t.getAccessibleColumnCount() == 4, "column count");

        javax.accessibility.Accessible cap = makeAccessible("caption");
        t.setAccessibleCaption(cap);
        check(t.getAccessibleCaption() == cap, "set/get caption");
        javax.accessibility.Accessible sum = makeAccessible("summary");
        t.setAccessibleSummary(sum);
        check(t.getAccessibleSummary() == sum, "set/get summary");

        check(t.getAccessibleAt(1, 2) != null, "getAccessibleAt non-null");
        check(t.getAccessibleRowExtentAt(0, 0) == 1, "row extent");
        check(t.getAccessibleColumnExtentAt(0, 0) == 1, "column extent");

        t.setAccessibleRowHeader(t);
        check(t.getAccessibleRowHeader() == t, "set/get row header");
        t.setAccessibleColumnHeader(t);
        check(t.getAccessibleColumnHeader() == t, "set/get column header");

        javax.accessibility.Accessible rd = makeAccessible("row0");
        t.setAccessibleRowDescription(0, rd);
        check(t.getAccessibleRowDescription(0) == rd, "set/get row description");
        javax.accessibility.Accessible cd = makeAccessible("col0");
        t.setAccessibleColumnDescription(0, cd);
        check(t.getAccessibleColumnDescription(0) == cd, "set/get column description");

        check(t.isAccessibleSelected(0, 0), "cell selected");
        check(!t.isAccessibleSelected(1, 1), "cell not selected");
        check(t.isAccessibleRowSelected(0), "row selected");
        check(!t.isAccessibleRowSelected(1), "row not selected");
        check(t.isAccessibleColumnSelected(0), "column selected");
        check(t.getSelectedAccessibleRows().length == 1 && t.getSelectedAccessibleRows()[0] == 0, "selected rows");
        check(t.getSelectedAccessibleColumns().length == 1 && t.getSelectedAccessibleColumns()[0] == 0, "selected columns");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
