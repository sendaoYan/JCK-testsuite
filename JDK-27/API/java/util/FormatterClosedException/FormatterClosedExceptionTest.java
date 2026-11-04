/*
 * @test
 * @summary java.util.FormatterClosedException conformance: hierarchy (extends
 *          IllegalStateException), no-arg constructor, trigger from a closed
 *          Formatter (format/flush/toString), message null, serialization round-trip.
 * @run main FormatterClosedExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Formatter;
import java.util.FormatterClosedException;

public class FormatterClosedExceptionTest {
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
        throw new AssertionError(msg + ": nothing thrown");
    }

    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("java.util.FormatterClosedException");
        check(c.getSuperclass() == IllegalStateException.class,
              "extends IllegalStateException");
        check(RuntimeException.class.isAssignableFrom(c), "unchecked");
        check(Serializable.class.isAssignableFrom(c), "Serializable");
        // 只有无参构造器
        check(c.getDeclaredConstructors().length == 1, "single no-arg constructor");
    }

    static void testConstructorAndState() {
        FormatterClosedException e = new FormatterClosedException();
        check(e.getMessage() == null, "message is null");
        check(e.getCause() == null, "cause is null");
        check(e.toString().contains("FormatterClosedException"), "toString names the class");
        FormatterClosedException e2 = new FormatterClosedException();
        check(e != e2, "fresh instance each construction");
        RuntimeException asRt = e;
        check(asRt instanceof IllegalStateException, "usable as ISE");
    }

    static void testFormatterTrigger() {
        Formatter f = new Formatter();
        f.format("x");
        f.close();
        expect(FormatterClosedException.class, () -> f.format("%d", 1), "format after close");
        expect(FormatterClosedException.class, f::flush, "flush after close");
        expect(FormatterClosedException.class, f::toString, "toString after close");
        // double close 幂等不抛
        f.close();
        check(true, "double close idempotent");
    }

    static void testSerializationRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(new FormatterClosedException());
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof FormatterClosedException, "round-trip preserves class");
        check(((FormatterClosedException) back).getMessage() == null, "round-trip message null");
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testConstructorAndState();
        testFormatterTrigger();
        testSerializationRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
