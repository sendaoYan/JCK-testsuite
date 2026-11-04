/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedTable conformance: white-box structure + black-box behavior.
 * @run main AccessibleExtendedTableTest
 */

import java.lang.reflect.*;

public class AccessibleExtendedTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static javax.accessibility.Accessible makeAccessible(final String tag) {
        return new javax.accessibility.Accessible() {
            public javax.accessibility.AccessibleContext getAccessibleContext() { return null; }
        };
    }

    static class Model implements javax.accessibility.AccessibleExtendedTable {
        int rows = 3, cols = 4;
        public int getAccessibleRow(int index) { return index / cols; }
        public int getAccessibleColumn(int index) { return index % cols; }
        public int getAccessibleIndex(int r, int c) { return r * cols + c; }
        // AccessibleTable
        public javax.accessibility.Accessible getAccessibleCaption() { return null; }
        public void setAccessibleCaption(javax.accessibility.Accessible a) { }
        public javax.accessibility.Accessible getAccessibleSummary() { return null; }
        public void setAccessibleSummary(javax.accessibility.Accessible a) { }
        public int getAccessibleRowCount() { return rows; }
        public int getAccessibleColumnCount() { return cols; }
        public javax.accessibility.Accessible getAccessibleAt(int r, int c) { return makeAccessible(r + "," + c); }
        public int getAccessibleRowExtentAt(int r, int c) { return 1; }
        public int getAccessibleColumnExtentAt(int r, int c) { return 1; }
        public javax.accessibility.AccessibleTable getAccessibleRowHeader() { return null; }
        public void setAccessibleRowHeader(javax.accessibility.AccessibleTable t) { }
        public javax.accessibility.AccessibleTable getAccessibleColumnHeader() { return null; }
        public void setAccessibleColumnHeader(javax.accessibility.AccessibleTable t) { }
        public javax.accessibility.Accessible getAccessibleRowDescription(int r) { return null; }
        public void setAccessibleRowDescription(int r, javax.accessibility.Accessible a) { }
        public javax.accessibility.Accessible getAccessibleColumnDescription(int c) { return null; }
        public void setAccessibleColumnDescription(int c, javax.accessibility.Accessible a) { }
        public boolean isAccessibleSelected(int r, int c) { return false; }
        public boolean isAccessibleRowSelected(int r) { return false; }
        public boolean isAccessibleColumnSelected(int c) { return false; }
        public int[] getSelectedAccessibleRows() { return new int[0]; }
        public int[] getSelectedAccessibleColumns() { return new int[0]; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedTable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.accessibility.AccessibleTable"), "extends AccessibleTable");
        check(hasMethod(c, "getAccessibleRow"), "declares getAccessibleRow");
        check(hasMethod(c, "getAccessibleColumn"), "declares getAccessibleColumn");
        check(hasMethod(c, "getAccessibleIndex"), "declares getAccessibleIndex");
    }

    static void testBlackBox() throws Exception {
        Model m = new Model();
        check(m.getAccessibleRowCount() == 3 && m.getAccessibleColumnCount() == 4, "inherited dimensions");
        check(m.getAccessibleIndex(1, 2) == 6, "getAccessibleIndex(1,2)==6");
        check(m.getAccessibleRow(6) == 1, "getAccessibleRow(6)==1");
        check(m.getAccessibleColumn(6) == 2, "getAccessibleColumn(6)==2");

        // round trip index -> (row,col) -> index
        for (int i = 0; i < 12; i++) {
            int r = m.getAccessibleRow(i);
            int c = m.getAccessibleColumn(i);
            check(m.getAccessibleIndex(r, c) == i, "index round trip " + i);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
