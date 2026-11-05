/*
 * @test
 * @summary javax.accessibility.AccessibleTableModelChange conformance: interface reflection.
 * @run main AccessibleTableModelChangeTest
 */

public class AccessibleTableModelChangeTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleTableModelChange");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getFirstRow"), "method getFirstRow");
        check(hasMethod(c, "getLastRow"), "method getLastRow");
        check(hasMethod(c, "getFirstColumn"), "method getFirstColumn");
        check(hasMethod(c, "getLastColumn"), "method getLastColumn");
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
