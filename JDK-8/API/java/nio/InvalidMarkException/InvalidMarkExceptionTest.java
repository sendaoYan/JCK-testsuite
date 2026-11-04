/*
 * @test
 * @summary java.nio.InvalidMarkException conformance: white-box (extends
 *          IllegalStateException, no-arg constructor) plus black-box (thrown on reset
 *          without a mark, and after mark discarded by position/limit/clear/flip/rewind).
 * @run main InvalidMarkExceptionTest
 */

import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;
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

public class InvalidMarkExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.InvalidMarkException");
        check(c.getSuperclass() == java.lang.IllegalStateException.class,
              "extends IllegalStateException");
        check(c.getConstructor() != null, "has no-arg constructor");
        check(java.io.Serializable.class.isAssignableFrom(c), "is Serializable");
        InvalidMarkException e = new InvalidMarkException();
        check(e.getMessage() == null, "default message is null");
    }

    static void testBlackBox() {
        ByteBuffer buf = ByteBuffer.allocate(4);
        checkThrows(InvalidMarkException.class, () -> buf.reset(), "reset without mark");

        buf.position(3).mark();
        buf.position(1);
        checkThrows(InvalidMarkException.class, () -> buf.reset(),
                "mark discarded when position set below mark");

        ByteBuffer b2 = ByteBuffer.allocate(4);
        b2.position(3).mark();
        b2.limit(2);
        checkThrows(InvalidMarkException.class, () -> b2.reset(),
                "mark discarded when limit set below mark");

        ByteBuffer b3 = ByteBuffer.allocate(4);
        b3.position(3).mark();
        b3.flip();
        checkThrows(InvalidMarkException.class, () -> b3.reset(), "flip discards mark");

        ByteBuffer b4 = ByteBuffer.allocate(4);
        b4.position(2).mark();
        b4.rewind();
        checkThrows(InvalidMarkException.class, () -> b4.reset(), "rewind discards mark");

        ByteBuffer b5 = ByteBuffer.allocate(4);
        b5.position(2).mark();
        b5.clear();
        checkThrows(InvalidMarkException.class, () -> b5.reset(), "clear discards mark");

        // reset() does not discard the mark; it may be reset again.
        ByteBuffer b6 = ByteBuffer.allocate(4);
        b6.position(2).mark();
        b6.reset();
        b6.reset();
        check(b6.position() == 2, "reset is repeatable");
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
        check(ObjectStreamClass.lookup(InvalidMarkException.class).getSerialVersionUID() == 1698329710438510774L,
                "serialVersionUID fixed value (identical JDK-8/JDK-27)");
        check(InvalidMarkException.class.getConstructors().length == 1,
                "exactly one public constructor (no-arg only)");
        try {
            InvalidMarkException.class.getConstructor(String.class);
            check(false, "no String-message constructor (java.nio style)");
        } catch (NoSuchMethodException e) {
            passed++;
        }
    }

    static InvalidMarkException captureTrigger() {
        try {
            ByteBuffer.allocate(2).reset();
            return null;
        } catch (InvalidMarkException e) {
            return e;
        }
    }

    static void testSerializationRoundTrip() throws Exception {
        InvalidMarkException fresh = new InvalidMarkException();
        check(fresh.toString().equals("java.nio.InvalidMarkException"),
                "toString is the class name (message is null)");
        InvalidMarkException thrown = captureTrigger();
        check(thrown != null, "library operation really throws InvalidMarkException");
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
        check(backFresh.getClass() == InvalidMarkException.class, "round-trip preserves the exact class");
        check(backThrown.getClass() == InvalidMarkException.class,
                "round-trip preserves a library-thrown instance's class");
        check(backFresh instanceof IllegalStateException,
                "round-trip keeps the superclass chain (IllegalStateException)");
        check(((InvalidMarkException) backFresh).getMessage() == null, "round-trip keeps message null");
        check(((InvalidMarkException) backThrown).getMessage() == null,
                "round-trip keeps the thrown instance's message null");
        check(((InvalidMarkException) backFresh).getCause() == null, "round-trip keeps cause null");
    }

    // Real trigger scenarios on typed buffers (precise exception types).
    static void testTypedBufferTriggers() {
        checkThrows(InvalidMarkException.class,
                () -> ShortBuffer.allocate(2).reset(), "typed buffer reset without mark");
        IntBuffer ib = IntBuffer.allocate(4);
        ib.position(2);
        ib.mark();
        ib.position(1);                        // below mark: mark discarded
        checkThrows(InvalidMarkException.class, ib::reset,
                "position moved below mark discards mark");
        LongBuffer lb = LongBuffer.allocate(4);
        lb.position(3);
        lb.mark();
        lb.limit(2);                           // below mark: mark discarded
        checkThrows(InvalidMarkException.class, lb::reset,
                "limit moved below mark discards mark");
        FloatBuffer fb = FloatBuffer.allocate(4);
        fb.position(1);
        fb.mark();
        checkThrows(InvalidMarkException.class, () -> fb.slice().reset(),
                "slice starts with an undefined mark");
        DoubleBuffer db = DoubleBuffer.allocate(4);
        db.position(2);
        db.mark();
        db.position(3);                        // forward: mark retained
        db.reset();
        check(db.position() == 2, "forward position then reset succeeds");
        db.position(3);
        check(db.duplicate().reset().position() == 2,
                "duplicate inherits the mark (contrast with slice)");
    }
}
