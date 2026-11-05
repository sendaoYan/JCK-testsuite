/*
 * @test
 * @summary javax.accessibility.AccessibleStreamable conformance: interface reflection.
 * @run main AccessibleStreamableTest
 */

public class AccessibleStreamableTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleStreamable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getMimeTypes"), "method getMimeTypes");
        check(hasMethod(c, "getStream"), "method getStream");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass ----
    static void testContractDeep() throws Exception {
        check(javax.accessibility.AccessibleStreamable.class.isInterface(), "interface");
        final java.io.ByteArrayInputStream data = new java.io.ByteArrayInputStream(new byte[] { 1, 2, 3 });
        java.awt.datatransfer.DataFlavor plain = new java.awt.datatransfer.DataFlavor("text/plain; class=java.lang.String", "Plain Text");
        javax.accessibility.AccessibleStreamable s = new javax.accessibility.AccessibleStreamable() {
            public java.awt.datatransfer.DataFlavor[] getMimeTypes() { return new java.awt.datatransfer.DataFlavor[] { plain }; }
            public java.io.InputStream getStream(java.awt.datatransfer.DataFlavor flavor) { return plain.equals(flavor) ? data : null; }
        };
        check(s.getStream(plain) == data, "matching flavor returns stream");
        check(s.getStream(new java.awt.datatransfer.DataFlavor("image/png", "PNG")) == null, "other flavor null");
        javax.accessibility.AccessibleStreamable empty = new javax.accessibility.AccessibleStreamable() {
            public java.awt.datatransfer.DataFlavor[] getMimeTypes() { return new java.awt.datatransfer.DataFlavor[0]; }
            public java.io.InputStream getStream(java.awt.datatransfer.DataFlavor flavor) { return null; }
        };
        check(empty.getStream(plain) == null, "null stream allowed");
        check(javax.accessibility.AccessibleStreamable.class.getMethod("getStream", java.awt.datatransfer.DataFlavor.class).getReturnType() == java.io.InputStream.class, "getStream(DataFlavor)->InputStream");
        check(javax.accessibility.AccessibleStreamable.class.getMethod("getMimeTypes").getReturnType() == java.awt.datatransfer.DataFlavor[].class, "getMimeTypes()->DataFlavor[]");
        check(s.getMimeTypes().length == 1 && plain.equals(s.getMimeTypes()[0]), "mime types passthrough");
        check(empty.getMimeTypes().length == 0, "empty mime types");
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
