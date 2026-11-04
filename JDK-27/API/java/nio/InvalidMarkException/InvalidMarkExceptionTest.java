/*
 * @test
 * @summary java.nio.InvalidMarkException conformance: white-box structure + black-box ctor chain and trigger.
 * @run main InvalidMarkExceptionTest
 */

import java.lang.reflect.Modifier;
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
        Class<?> c = Class.forName("java.nio.InvalidMarkException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == IllegalStateException.class, "superclass is IllegalStateException");
        check(c.getConstructor() != null, "no-arg constructor present");
    }

    static void testBlackBox() {
        InvalidMarkException e = new InvalidMarkException();
        check(e instanceof IllegalStateException, "is IllegalStateException");
        check(e instanceof RuntimeException, "is RuntimeException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        ByteBuffer buf = ByteBuffer.allocate(4);
        expect(InvalidMarkException.class, () -> buf.reset(), "reset without a mark throws");

        // mark then reset works, then clearing (discards mark) and resetting throws again.
        buf.mark();
        buf.position(2);
        buf.reset();
        check(buf.position() == 0, "reset returns to marked position");
        buf.clear();
        expect(InvalidMarkException.class, () -> buf.reset(), "reset after clear (mark discarded) throws");
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
        expect(InvalidMarkException.class,
                () -> ShortBuffer.allocate(2).reset(), "typed buffer reset without mark");
        IntBuffer ib = IntBuffer.allocate(4);
        ib.position(2);
        ib.mark();
        ib.position(1);                        // below mark: mark discarded
        expect(InvalidMarkException.class, ib::reset,
                "position moved below mark discards mark");
        LongBuffer lb = LongBuffer.allocate(4);
        lb.position(3);
        lb.mark();
        lb.limit(2);                           // below mark: mark discarded
        expect(InvalidMarkException.class, lb::reset,
                "limit moved below mark discards mark");
        FloatBuffer fb = FloatBuffer.allocate(4);
        fb.position(1);
        fb.mark();
        expect(InvalidMarkException.class, () -> fb.slice().reset(),
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
