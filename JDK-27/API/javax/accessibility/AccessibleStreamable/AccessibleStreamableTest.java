/*
 * @test
 * @summary javax.accessibility.AccessibleStreamable conformance: white-box structure + black-box behavior.
 * @run main AccessibleStreamableTest
 */

import java.lang.reflect.*;
import java.awt.datatransfer.DataFlavor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AccessibleStreamableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleStreamable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getMimeTypes"), "declares getMimeTypes");
        check(hasMethod(c, "getStream"), "declares getStream");
    }

    static void testBlackBox() throws Exception {
        final DataFlavor[] flavors = new DataFlavor[]{
            new DataFlavor("text/plain; charset=UTF-8", "plain text"),
            new DataFlavor("image/png", "png image")
        };
        final InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        javax.accessibility.AccessibleStreamable s = new javax.accessibility.AccessibleStreamable() {
            public DataFlavor[] getMimeTypes() { return flavors; }
            public InputStream getStream(DataFlavor f) { return stream; }
        };

        DataFlavor[] got = s.getMimeTypes();
        check(got.length == 2, "mimeTypes length 2");
        check(got[0] == flavors[0], "mimeTypes[0] identity");
        check(s.getStream(flavors[0]) == stream, "getStream returns same stream");

        javax.accessibility.AccessibleStreamable empty = new javax.accessibility.AccessibleStreamable() {
            public DataFlavor[] getMimeTypes() { return new DataFlavor[0]; }
            public InputStream getStream(DataFlavor f) { return null; }
        };
        check(empty.getMimeTypes().length == 0, "empty mimeTypes");
        check(empty.getStream(null) == null, "null stream allowed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
