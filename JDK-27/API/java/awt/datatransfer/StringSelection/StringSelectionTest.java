/*
 * @test
 * @summary java.awt.datatransfer.StringSelection conformance: white-box structure + black-box behavior.
 * @run main StringSelectionTest
 */

import java.lang.reflect.*;

public class StringSelectionTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.StringSelection", false, StringSelectionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.StringSelection");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.awt.datatransfer.Transferable"), "implements Transferable");
        check(hasInterface(c, "java.awt.datatransfer.ClipboardOwner"), "implements ClipboardOwner");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.StringSelection s = new java.awt.datatransfer.StringSelection("hello world");
        java.awt.datatransfer.DataFlavor[] fl = s.getTransferDataFlavors();
        check(fl != null && fl.length >= 2, "has stringFlavor + plainTextFlavor");
        check(s.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor), "supports stringFlavor");
        check(!s.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.imageFlavor), "not imageFlavor");
        check("hello world".equals(s.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor)), "getTransferData string");
        Object text = s.getTransferData(java.awt.datatransfer.DataFlavor.plainTextFlavor);
        check(text != null, "plainText data non-null");
        boolean unsup = false;
        try { s.getTransferData(java.awt.datatransfer.DataFlavor.imageFlavor); }
        catch (java.awt.datatransfer.UnsupportedFlavorException e) { unsup = true; }
        check(unsup, "UnsupportedFlavorException for imageFlavor");
        s.lostOwnership(null, null);
        check(true, "lostOwnership callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
