/*
 * @test
 * @summary java.awt.datatransfer.UnsupportedFlavorException conformance: white-box structure + black-box behavior.
 * @run main UnsupportedFlavorExceptionTest
 */

import java.io.*;
import java.lang.reflect.*;

public class UnsupportedFlavorExceptionTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.UnsupportedFlavorException", false, UnsupportedFlavorExceptionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.UnsupportedFlavorException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getDeclaredConstructors().length == 1, "single constructor");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.UnsupportedFlavorException e = new java.awt.datatransfer.UnsupportedFlavorException(java.awt.datatransfer.DataFlavor.imageFlavor);
        check(e.getMessage() != null, "getMessage non-null");
        check(java.lang.Exception.class.isInstance(e), "instanceof Exception");
        check(e instanceof java.io.Serializable, "instanceof Serializable");
        // message is the flavor's human presentable name
        check(java.awt.datatransfer.DataFlavor.imageFlavor.getHumanPresentableName().equals(e.getMessage()), "message is flavor human presentable name");
        // custom flavor carries its own name
        java.awt.datatransfer.DataFlavor custom = new java.awt.datatransfer.DataFlavor("application/x-test", "Test Data");
        java.awt.datatransfer.UnsupportedFlavorException e2 = new java.awt.datatransfer.UnsupportedFlavorException(custom);
        check("Test Data".equals(e2.getMessage()), "message follows custom human presentable name");

        // thrown from Transferable.getTransferData and caught precisely
        java.awt.datatransfer.Transferable t = new java.awt.datatransfer.StringSelection("x");
        boolean caught = false;
        String msg = null;
        try { t.getTransferData(java.awt.datatransfer.DataFlavor.imageFlavor); }
        catch (java.awt.datatransfer.UnsupportedFlavorException x) { caught = true; msg = x.getMessage(); }
        catch (java.io.IOException x) { /* not expected here */ }
        check(caught, "StringSelection.getTransferData throws UnsupportedFlavorException");
        check(msg != null && msg.length() > 0, "thrown exception carries non-empty message");

        // serialization round-trip preserves message
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        new ObjectOutputStream(b).writeObject(e);
        java.awt.datatransfer.UnsupportedFlavorException back =
            (java.awt.datatransfer.UnsupportedFlavorException) new ObjectInputStream(new ByteArrayInputStream(b.toByteArray())).readObject();
        check(back.getMessage() != null && back.getMessage().equals(e.getMessage()), "serialization preserves message");
        // toString includes class name
        check(e.toString().contains("UnsupportedFlavorException"), "toString has class name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
