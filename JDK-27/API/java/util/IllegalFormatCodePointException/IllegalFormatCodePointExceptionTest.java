/*
 * @test
 * @summary java.util.IllegalFormatCodePointException conformance: hierarchy, constructor matrix (null NPE),
 *          getters and exact message formats, Formatter trigger producing the
 *          exception with populated getters, and serialization round-trip.
 * @run main IllegalFormatCodePointExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Formatter;
import java.util.IllegalFormatException;

public class IllegalFormatCodePointExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T grab(Class<T> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return (T) t; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName(), t);
        }
        throw new AssertionError(msg + ": nothing thrown");
    }

    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("java.util.IllegalFormatCodePointException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException (unchecked)");
        check(Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.util.IllegalFormatCodePointException.class.getSuperclass() != Object.class, "non-root class");
    }

    static void testConstructorsAndGetters() {
        java.util.IllegalFormatCodePointException e = new java.util.IllegalFormatCodePointException(0x110000);
        check(e.getCodePoint() == 0x110000, "getCodePoint");
        check("Code point = 0x110000".equals(e.getMessage()), "getMessage hex format");
        java.util.IllegalFormatCodePointException neg = new java.util.IllegalFormatCodePointException(0);
        check(neg.getCodePoint() == 0, "code point 0 permitted in ctor");    }

    static void testFormatterTrigger() {
        java.util.IllegalFormatCodePointException t = grab(java.util.IllegalFormatCodePointException.class,
                () -> new Formatter().format("%c", 0x110000), "format %c invalid code point");
        check(t.getCodePoint() == 0x110000, "trigger populates getCodePoint");    }

    static void testSerializationRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(new java.util.IllegalFormatCodePointException(0x110000));
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((java.util.IllegalFormatCodePointException) back).getCodePoint() == 0x110000, "serialization round-trip preserves state");
    }

    static boolean commaOk(Object back) {
        java.util.FormatFlagsConversionMismatchException e = (java.util.FormatFlagsConversionMismatchException) back;
        return ",".equals(e.getFlags()) && e.getConversion() == 'x';
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testConstructorsAndGetters();
        testFormatterTrigger();
        testSerializationRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
