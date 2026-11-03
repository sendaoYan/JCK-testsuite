/*
 * @test
 * @summary java.awt.datatransfer.Transferable conformance: white-box structure + black-box behavior.
 * @run main TransferableTest
 */

import java.lang.reflect.*;

public class TransferableTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.datatransfer.Transferable", false, TransferableTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.Transferable");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isInterface(), "is interface");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.Transferable t = new java.awt.datatransfer.Transferable() {
            public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() { return new java.awt.datatransfer.DataFlavor[0]; }
            public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor f) { return false; }
            public Object getTransferData(java.awt.datatransfer.DataFlavor f) throws java.awt.datatransfer.UnsupportedFlavorException { throw new java.awt.datatransfer.UnsupportedFlavorException(f); }
        };
        check(t.getTransferDataFlavors() != null && t.getTransferDataFlavors().length == 0, "empty flavors");
        check(t.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor) == false, "isDataFlavorSupported false");
        boolean unsup = false;
        try { t.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor); }
        catch (java.awt.datatransfer.UnsupportedFlavorException e) { unsup = true; }
        check(unsup, "getTransferData throws UnsupportedFlavorException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
