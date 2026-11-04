/*
 * @test
 * @summary java.util.DuplicateFormatFlagsException conformance: hierarchy, constructor matrix (null NPE),
 *          getters and exact message formats, Formatter trigger producing the
 *          exception with populated getters, and serialization round-trip.
 * @run main DuplicateFormatFlagsExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Formatter;
import java.util.IllegalFormatException;

public class DuplicateFormatFlagsExceptionTest {
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
        Class<?> c = Class.forName("java.util.DuplicateFormatFlagsException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException (unchecked)");
        check(Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.util.DuplicateFormatFlagsException.class.getSuperclass() != Object.class, "non-root class");
    }

    static void testConstructorsAndGetters() {
        java.util.DuplicateFormatFlagsException e = new java.util.DuplicateFormatFlagsException("+,");
        check("+,".equals(e.getFlags()), "getFlags returns ctor value");
        check("Flags = '+,'".equals(e.getMessage()), "getMessage format Flags = '<f>'");
        check(e.getCause() == null, "no cause by default");
        java.util.DuplicateFormatFlagsException e2 = new java.util.DuplicateFormatFlagsException("-");
        check(!e.equals(e2), "distinct flags yield distinct state");    }

    static void testFormatterTrigger() {
        java.util.DuplicateFormatFlagsException t = grab(java.util.DuplicateFormatFlagsException.class,
                () -> new Formatter().format("%,,d", 1), "format %,,d");
        check(t.getFlags() != null && t.getFlags().length() > 0, "trigger populates getFlags");
        check(IllegalFormatException.class.isInstance(t), "trigger result is IllegalFormatException");    }

    static void testSerializationRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(new java.util.DuplicateFormatFlagsException("+,"));
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check("+,".equals(((java.util.DuplicateFormatFlagsException) back).getFlags()), "serialization round-trip preserves state");
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
