/*
 * @test
 * @summary java.util.IllegalFormatConversionException conformance: hierarchy, constructor matrix (null NPE),
 *          getters and exact message formats, Formatter trigger producing the
 *          exception with populated getters, and serialization round-trip.
 * @run main IllegalFormatConversionExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Formatter;
import java.util.IllegalFormatException;

public class IllegalFormatConversionExceptionTest {
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
        Class<?> c = Class.forName("java.util.IllegalFormatConversionException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException (unchecked)");
        check(Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.util.IllegalFormatConversionException.class.getSuperclass() != Object.class, "non-root class");
    }

    static void testConstructorsAndGetters() {
        java.util.IllegalFormatConversionException e =
                new java.util.IllegalFormatConversionException('d', String.class);
        check('d' == e.getConversion(), "getConversion");
        check(e.getArgumentClass() == String.class, "getArgumentClass");
        check("d != java.lang.String".equals(e.getMessage()), "getMessage format conv != class");
        expect(NullPointerException.class, () -> new java.util.IllegalFormatConversionException('d', null),
               "null argument class NPE");    }

    static void testFormatterTrigger() {
        java.util.IllegalFormatConversionException t = grab(java.util.IllegalFormatConversionException.class,
                () -> new Formatter().format("%d", "str"), "format %d with String");
        check(t.getArgumentClass() == String.class && t.getConversion() == 'd', "trigger populates getters");    }

    static void testSerializationRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(new java.util.IllegalFormatConversionException('d', String.class));
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(((java.util.IllegalFormatConversionException) back).getArgumentClass() == String.class, "serialization round-trip preserves state");
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
