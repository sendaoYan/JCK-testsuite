/*
 * @test
 * @summary java.nio.BufferUnderflowException conformance: white-box structure + black-box ctor chain and trigger.
 * @run main BufferUnderflowExceptionTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public class BufferUnderflowExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.BufferUnderflowException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getConstructor() != null, "no-arg constructor present");
    }

    static void testBlackBox() {
        BufferUnderflowException e = new BufferUnderflowException();
        check(e instanceof RuntimeException, "is RuntimeException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put((byte) 1);
        bb.flip();
        check(bb.get() == 1, "read the single byte");
        expect(BufferUnderflowException.class, () -> bb.get(), "get past limit throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testClassContract();
        testSerializationRoundTrip();
        testTypedBufferTriggers();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Batch A2 deepening: fixed serialVersionUID (ObjectStreamClass; the same
    // values were probed on JDK-8 jdk8u372-b07 and JDK-27 27.0.1.0.2+1), the
    // constructor surface (java.nio exceptions expose the no-arg ctor only),
    // serialization round-trip of fresh and library-thrown instances. ----

    static void testClassContract() throws Exception {
        check(ObjectStreamClass.lookup(BufferUnderflowException.class).getSerialVersionUID() == -1713313658691622206L,
                "serialVersionUID fixed value (identical JDK-8/JDK-27)");
        check(BufferUnderflowException.class.getConstructors().length == 1,
                "exactly one public constructor (no-arg only)");
        try {
            BufferUnderflowException.class.getConstructor(String.class);
            check(false, "no String-message constructor (java.nio style)");
        } catch (NoSuchMethodException e) {
            passed++;
        }
    }

    static BufferUnderflowException captureTrigger() {
        try {
            ByteBuffer.allocate(0).get();
            return null;
        } catch (BufferUnderflowException e) {
            return e;
        }
    }

    static void testSerializationRoundTrip() throws Exception {
        BufferUnderflowException fresh = new BufferUnderflowException();
        check(fresh.toString().equals("java.nio.BufferUnderflowException"),
                "toString is the class name (message is null)");
        BufferUnderflowException thrown = captureTrigger();
        check(thrown != null, "library operation really throws BufferUnderflowException");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(fresh);
        oos.writeObject(thrown);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        Object backFresh = ois.readObject();
        Object backThrown = ois.readObject();
        ois.close();
        check(backFresh.getClass() == BufferUnderflowException.class, "round-trip preserves the exact class");
        check(backThrown.getClass() == BufferUnderflowException.class,
                "round-trip preserves a library-thrown instance's class");
        check(backFresh instanceof RuntimeException,
                "round-trip keeps the superclass chain (RuntimeException)");
        check(((BufferUnderflowException) backFresh).getMessage() == null, "round-trip keeps message null");
        check(((BufferUnderflowException) backThrown).getMessage() == null,
                "round-trip keeps the thrown instance's message null");
        check(((BufferUnderflowException) backFresh).getCause() == null, "round-trip keeps cause null");
    }

    // Real trigger scenarios on typed buffers (precise exception types).
    static void testTypedBufferTriggers() {
        ShortBuffer sb = ShortBuffer.allocate(2);
        sb.flip();
        expect(BufferUnderflowException.class, sb::get,
                "ShortBuffer relative get on empty buffer");
        IntBuffer ib = IntBuffer.wrap(new int[] {1, 2});
        ib.flip();
        expect(BufferUnderflowException.class, () -> ib.get(new int[3]),
                "IntBuffer bulk get past remaining");
        FloatBuffer fb = FloatBuffer.allocate(3);
        fb.put(1f).put(2f).put(3f);
        fb.flip();
        fb.position(2);                        // one element unread
        fb.compact();                          // pos=1, limit=3
        check(fb.remaining() == 2, "compacted buffer keeps limit-pos readable");
        fb.get();
        fb.get();
        expect(BufferUnderflowException.class, fb::get,
                "FloatBuffer get past compacted remaining");
        DoubleBuffer db = DoubleBuffer.wrap(new double[] {1d});
        db.flip();
        expect(BufferUnderflowException.class, () -> db.get(new double[2]),
                "DoubleBuffer bulk get past remaining");
        check(db.position() == 0, "failed bulk get leaves position unchanged");
        expect(IndexOutOfBoundsException.class,
                () -> LongBuffer.allocate(0).get(new long[2], -1, 2),
                "bulk get bounds error outranks underflow");
    }
}
