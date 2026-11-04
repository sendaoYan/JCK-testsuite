/*
 * @test
 * @summary java.nio.ReadOnlyBufferException conformance: white-box structure + black-box ctor chain and trigger.
 * @run main ReadOnlyBufferExceptionTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ReadOnlyBufferExceptionTest {
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
        Class<?> c = Class.forName("java.nio.ReadOnlyBufferException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == UnsupportedOperationException.class, "superclass is UnsupportedOperationException");
        check(c.getConstructor() != null, "no-arg constructor present");
    }

    static void testBlackBox() {
        ReadOnlyBufferException e = new ReadOnlyBufferException();
        check(e instanceof UnsupportedOperationException, "is UnsupportedOperationException");
        check(e instanceof RuntimeException, "is RuntimeException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        ByteBuffer ro = ByteBuffer.allocate(4).asReadOnlyBuffer();
        check(ro.isReadOnly(), "buffer is read-only");
        expect(ReadOnlyBufferException.class, () -> ro.put((byte) 1), "put on read-only buffer throws");
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
        check(ObjectStreamClass.lookup(ReadOnlyBufferException.class).getSerialVersionUID() == -1210063976496234090L,
                "serialVersionUID fixed value (identical JDK-8/JDK-27)");
        check(ReadOnlyBufferException.class.getConstructors().length == 1,
                "exactly one public constructor (no-arg only)");
        try {
            ReadOnlyBufferException.class.getConstructor(String.class);
            check(false, "no String-message constructor (java.nio style)");
        } catch (NoSuchMethodException e) {
            passed++;
        }
    }

    static ReadOnlyBufferException captureTrigger() {
        try {
            ByteBuffer.allocate(2).asReadOnlyBuffer().put((byte) 1);
            return null;
        } catch (ReadOnlyBufferException e) {
            return e;
        }
    }

    static void testSerializationRoundTrip() throws Exception {
        ReadOnlyBufferException fresh = new ReadOnlyBufferException();
        check(fresh.toString().equals("java.nio.ReadOnlyBufferException"),
                "toString is the class name (message is null)");
        ReadOnlyBufferException thrown = captureTrigger();
        check(thrown != null, "library operation really throws ReadOnlyBufferException");
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
        check(backFresh.getClass() == ReadOnlyBufferException.class, "round-trip preserves the exact class");
        check(backThrown.getClass() == ReadOnlyBufferException.class,
                "round-trip preserves a library-thrown instance's class");
        check(backFresh instanceof UnsupportedOperationException,
                "round-trip keeps the superclass chain (UnsupportedOperationException)");
        check(((ReadOnlyBufferException) backFresh).getMessage() == null, "round-trip keeps message null");
        check(((ReadOnlyBufferException) backThrown).getMessage() == null,
                "round-trip keeps the thrown instance's message null");
        check(((ReadOnlyBufferException) backFresh).getCause() == null, "round-trip keeps cause null");
    }

    // Real trigger scenarios on typed buffers (precise exception types).
    static void testTypedBufferTriggers() {
        ShortBuffer ro = ShortBuffer.wrap(new short[] {1, 2}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "asReadOnlyBuffer marks the view read-only");
        expect(ReadOnlyBufferException.class, () -> ro.put((short) 1),
                "ShortBuffer read-only relative put");
        expect(ReadOnlyBufferException.class, () -> ro.put(0, (short) 1),
                "ShortBuffer read-only absolute put");
        expect(ReadOnlyBufferException.class, () -> ro.put(new short[] {1}, 0, 1),
                "ShortBuffer read-only bulk put");
        expect(ReadOnlyBufferException.class,
                () -> ro.put(ShortBuffer.wrap(new short[] {1})),
                "ShortBuffer read-only put(XBuffer)");
        expect(ReadOnlyBufferException.class, () -> ro.compact(),
                "ShortBuffer read-only compact");
        expect(ReadOnlyBufferException.class, () -> ro.array(),
                "ShortBuffer read-only array()");
        FloatBuffer rof = ByteBuffer.allocate(4).asReadOnlyBuffer().asFloatBuffer();
        check(rof.isReadOnly(), "read-only byte buffer gives a read-only float view");
        expect(ReadOnlyBufferException.class, () -> rof.put(1f),
                "read-only float view put");
        ro.position(1);
        ro.limit(2);
        ro.mark();
        check(ro.position() == 1 && ro.limit() == 2, "cursor stays mutable when read-only");
        check(ro.get(0) == 1, "read-only buffer reads still work");
    }
}
