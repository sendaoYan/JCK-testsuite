/*
 * @test
 * @summary javax.accessibility.AccessibleTableModelChange conformance: white-box structure + black-box behavior.
 * @run main AccessibleTableModelChangeTest
 */

import java.lang.reflect.*;

public class AccessibleTableModelChangeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleTableModelChange");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getType"), "declares getType");
        check(hasMethod(c, "getFirstRow"), "declares getFirstRow");
        check(hasMethod(c, "getLastRow"), "declares getLastRow");
        check(hasMethod(c, "getFirstColumn"), "declares getFirstColumn");
        check(hasMethod(c, "getLastColumn"), "declares getLastColumn");
    }

    static void testBlackBox() throws Exception {
        check(javax.accessibility.AccessibleTableModelChange.INSERT == 1, "INSERT == 1");
        check(javax.accessibility.AccessibleTableModelChange.UPDATE == 0, "UPDATE == 0");
        check(javax.accessibility.AccessibleTableModelChange.DELETE == -1, "DELETE == -1");

        javax.accessibility.AccessibleTableModelChange ins = new javax.accessibility.AccessibleTableModelChange() {
            public int getType() { return INSERT; }
            public int getFirstRow() { return 0; }
            public int getLastRow() { return 1; }
            public int getFirstColumn() { return 2; }
            public int getLastColumn() { return 3; }
        };
        check(ins.getType() == javax.accessibility.AccessibleTableModelChange.INSERT, "impl getType");
        check(ins.getFirstRow() == 0, "impl getFirstRow");
        check(ins.getLastRow() == 1, "impl getLastRow");
        check(ins.getFirstColumn() == 2, "impl getFirstColumn");
        check(ins.getLastColumn() == 3, "impl getLastColumn");

        javax.accessibility.AccessibleTableModelChange del = new javax.accessibility.AccessibleTableModelChange() {
            public int getType() { return DELETE; }
            public int getFirstRow() { return 5; }
            public int getLastRow() { return 5; }
            public int getFirstColumn() { return 6; }
            public int getLastColumn() { return 6; }
        };
        check(del.getType() == javax.accessibility.AccessibleTableModelChange.DELETE, "delete impl getType");
        check(del.getFirstRow() == 5 && del.getLastRow() == 5, "delete impl rows");
        check(del.getFirstColumn() == 6 && del.getLastColumn() == 6, "delete impl columns");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: 3 constants + recording subclass ----
    static void testContractDeep() throws Exception {
        check(javax.accessibility.AccessibleTableModelChange.INSERT == 1, "INSERT 1");
        check(javax.accessibility.AccessibleTableModelChange.UPDATE == 0, "UPDATE 0");
        check(javax.accessibility.AccessibleTableModelChange.DELETE == -1, "DELETE -1");
        check(javax.accessibility.AccessibleTableModelChange.class.isInterface(), "interface");
        javax.accessibility.AccessibleTableModelChange ch = new javax.accessibility.AccessibleTableModelChange() {
            public int getType() { return javax.accessibility.AccessibleTableModelChange.INSERT; }
            public int getFirstRow() { return 1; }
            public int getLastRow() { return 3; }
            public int getFirstColumn() { return 0; }
            public int getLastColumn() { return 2; }
        };
        check(ch.getType() == 1 && ch.getFirstRow() == 1 && ch.getLastRow() == 3, "rows");
        check(ch.getFirstColumn() == 0 && ch.getLastColumn() == 2, "cols");
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
