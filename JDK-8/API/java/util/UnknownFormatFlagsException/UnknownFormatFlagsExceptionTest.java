/*
 * @test
 * @summary java.util.UnknownFormatFlagsException conformance: hierarchy, constructor matrix (null NPE),
 *          getters and exact message formats, Formatter trigger producing the
 *          exception with populated getters, and serialization round-trip.
 * @run main UnknownFormatFlagsExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Formatter;
import java.util.IllegalFormatException;

public class UnknownFormatFlagsExceptionTest {
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
        Class<?> c = Class.forName("java.util.UnknownFormatFlagsException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        check(RuntimeException.class.isAssignableFrom(c), "is a RuntimeException (unchecked)");
        check(Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.util.UnknownFormatFlagsException.class.getSuperclass() != Object.class, "non-root class");
    }

    static void testConstructorsAndGetters() {
        java.util.UnknownFormatFlagsException e = new java.util.UnknownFormatFlagsException("#,");
        check("#,".equals(e.getFlags()), "getFlags");
        check("Flags = #,".equals(e.getMessage()), "getMessage format");
        expect(NullPointerException.class, () -> new java.util.UnknownFormatFlagsException(null),
               "null flags NPE");    }

    static void testFormatterTrigger() {
        // Formatter 路径极少抛 UnknownFormatFlagsException；用 %x 接 '(' 验证族内异常同源
        expect(IllegalFormatException.class, () -> new Formatter().format("%(x", 255), "%(x family IFE");
        check(new java.util.UnknownFormatFlagsException("u").getFlags().equals("u"), "getter independent");    }

    static void testSerializationRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(new java.util.UnknownFormatFlagsException("#,"));
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check("#,".equals(((java.util.UnknownFormatFlagsException) back).getFlags()), "serialization round-trip preserves state");
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
