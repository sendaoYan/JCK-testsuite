/*
 * @test
 * @summary java.util.FormatFlagsConversionMismatchException conformance: hierarchy, constructor matrix (null NPE),
 *          getters and exact message formats, Formatter trigger producing the
 *          exception with populated getters, and serialization round-trip.
 * @run main FormatFlagsConversionMismatchExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Formatter;
import java.util.IllegalFormatException;

public class FormatFlagsConversionMismatchExceptionTest {
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
        Class<?> c = Class.forName("java.util.FormatFlagsConversionMismatchException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException (unchecked)");
        check(Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.util.FormatFlagsConversionMismatchException.class.getSuperclass() != Object.class, "non-root class");
    }

    static void testConstructorsAndGetters() {
        java.util.FormatFlagsConversionMismatchException e =
                new java.util.FormatFlagsConversionMismatchException(",", 'x');
        check(",".equals(e.getFlags()), "getFlags");
        check('x' == e.getConversion(), "getConversion");
        check("Conversion = x, Flags = ,".equals(e.getMessage()), "getMessage format");    }

    static void testFormatterTrigger() {
        java.util.FormatFlagsConversionMismatchException t =
                grab(java.util.FormatFlagsConversionMismatchException.class,
                     () -> new Formatter().format("%,x", 255), "format %,x");
        check(",".equals(t.getFlags()) && 'x' == t.getConversion(), "trigger populates both getters");    }

    static void testSerializationRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(new java.util.FormatFlagsConversionMismatchException(",", 'x'));
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(commaOk(back), "serialization round-trip preserves state");
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
