/*
 * @test
 * @summary java.nio.BufferOverflowException conformance: white-box structure + black-box ctor chain and trigger.
 * @run main BufferOverflowExceptionTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
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

public class BufferOverflowExceptionTest {
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
        Class<?> c = Class.forName("java.nio.BufferOverflowException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getConstructor() != null, "no-arg constructor present");
    }

    static void testBlackBox() {
        BufferOverflowException e = new BufferOverflowException();
        check(e instanceof RuntimeException, "is RuntimeException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put((byte) 1);
        expect(BufferOverflowException.class, () -> bb.put((byte) 2), "put past capacity throws");
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
        check(ObjectStreamClass.lookup(BufferOverflowException.class).getSerialVersionUID() == -5484897634319144535L,
                "serialVersionUID fixed value (identical JDK-8/JDK-27)");
        check(BufferOverflowException.class.getConstructors().length == 1,
                "exactly one public constructor (no-arg only)");
        try {
            BufferOverflowException.class.getConstructor(String.class);
            check(false, "no String-message constructor (java.nio style)");
        } catch (NoSuchMethodException e) {
            passed++;
        }
    }

    static BufferOverflowException captureTrigger() {
        try {
            ByteBuffer.allocate(0).put((byte) 1);
            return null;
        } catch (BufferOverflowException e) {
            return e;
        }
    }

    static void testSerializationRoundTrip() throws Exception {
        BufferOverflowException fresh = new BufferOverflowException();
        check(fresh.toString().equals("java.nio.BufferOverflowException"),
                "toString is the class name (message is null)");
        BufferOverflowException thrown = captureTrigger();
        check(thrown != null, "library operation really throws BufferOverflowException");
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
        check(backFresh.getClass() == BufferOverflowException.class, "round-trip preserves the exact class");
        check(backThrown.getClass() == BufferOverflowException.class,
                "round-trip preserves a library-thrown instance's class");
        check(backFresh instanceof RuntimeException,
                "round-trip keeps the superclass chain (RuntimeException)");
        check(((BufferOverflowException) backFresh).getMessage() == null, "round-trip keeps message null");
        check(((BufferOverflowException) backThrown).getMessage() == null,
                "round-trip keeps the thrown instance's message null");
        check(((BufferOverflowException) backFresh).getCause() == null, "round-trip keeps cause null");
    }

    // Real trigger scenarios on typed buffers (precise exception types).
    static void testTypedBufferTriggers() {
        ShortBuffer full = ShortBuffer.allocate(2);
        full.put((short) 1).put((short) 2);
        expect(BufferOverflowException.class, () -> full.put((short) 3),
                "ShortBuffer relative put on full buffer");
        IntBuffer ib = IntBuffer.allocate(1);
        expect(BufferOverflowException.class, () -> ib.put(new int[] {1, 2}, 0, 2),
                "IntBuffer bulk put past remaining");
        LongBuffer big = LongBuffer.wrap(new long[] {1L, 2L, 3L});
        LongBuffer small = LongBuffer.allocate(1);
        expect(BufferOverflowException.class, () -> small.put(big),
                "LongBuffer put(LongBuffer) with insufficient remaining");
        DoubleBuffer db = DoubleBuffer.allocate(1);
        db.put(1d);
        expect(BufferOverflowException.class, () -> db.put(2d),
                "DoubleBuffer relative put on full buffer");
        check(db.position() == 1, "failed typed put leaves position unchanged");
        expect(IndexOutOfBoundsException.class,
                () -> FloatBuffer.allocate(1).put(new float[] {1f, 2f}, -1, 2),
                "bulk put bounds error outranks overflow");
    }
}
