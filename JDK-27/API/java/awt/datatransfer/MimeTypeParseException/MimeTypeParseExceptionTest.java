/*
 * @test
 * @summary java.awt.datatransfer.MimeTypeParseException conformance: white-box structure + black-box behavior.
 * @run main MimeTypeParseExceptionTest
 */

import java.io.*;
import java.lang.reflect.*;

public class MimeTypeParseExceptionTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.MimeTypeParseException", false, MimeTypeParseExceptionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.MimeTypeParseException");
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
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "public no-arg ctor");
        check(Modifier.isPublic(c.getConstructor(String.class).getModifiers()), "public message ctor");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.MimeTypeParseException e1 = new java.awt.datatransfer.MimeTypeParseException();
        java.awt.datatransfer.MimeTypeParseException e2 = new java.awt.datatransfer.MimeTypeParseException("bad mime");
        check("bad mime".equals(e2.getMessage()), "message ctor");
        check(java.lang.Exception.class.isInstance(e2), "instanceof Exception");
        check(e1.getMessage() == null, "no-arg ctor message null");
        check(e1 instanceof java.io.Serializable, "instanceof Serializable");

        // message round-trips exactly, including empty and unicode
        check("".equals(new java.awt.datatransfer.MimeTypeParseException("").getMessage()), "empty message round-trip");
        String uni = "非法MIME";
        check(uni.equals(new java.awt.datatransfer.MimeTypeParseException(uni).getMessage()), "unicode message round-trip");
        // identity of the message object is preserved
        String msg = new String("shared");
        check(new java.awt.datatransfer.MimeTypeParseException(msg).getMessage() == msg, "message object identity preserved");

        // checked-exception semantics: callable from a method declaring it, catchable as Exception
        boolean caught = false;
        try { throwMime(); }
        catch (java.awt.datatransfer.MimeTypeParseException x) { caught = true; check("t".equals(x.getMessage()), "thrown message preserved"); }
        check(caught, "catchable as its own type");

        // serialization round-trip preserves message and type
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        new ObjectOutputStream(b).writeObject(e2);
        java.awt.datatransfer.MimeTypeParseException back =
            (java.awt.datatransfer.MimeTypeParseException) new ObjectInputStream(new ByteArrayInputStream(b.toByteArray())).readObject();
        check("bad mime".equals(back.getMessage()), "serialization preserves message");
        check(back instanceof java.awt.datatransfer.MimeTypeParseException, "serialization preserves type");
        // null message survives round-trip too
        b = new ByteArrayOutputStream();
        new ObjectOutputStream(b).writeObject(e1);
        java.awt.datatransfer.MimeTypeParseException back1 =
            (java.awt.datatransfer.MimeTypeParseException) new ObjectInputStream(new ByteArrayInputStream(b.toByteArray())).readObject();
        check(back1.getMessage() == null, "serialization preserves null message");

        // toString from Throwable includes class name and message
        check(e2.toString().contains("MimeTypeParseException"), "toString has class name");
        check(e2.toString().contains("bad mime"), "toString has message");
    }

    static void throwMime() throws java.awt.datatransfer.MimeTypeParseException {
        throw new java.awt.datatransfer.MimeTypeParseException("t");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
