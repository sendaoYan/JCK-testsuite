/*
 * @test
 * @summary java.nio.ReadOnlyBufferException conformance: white-box (extends
 *          UnsupportedOperationException, no-arg constructor) plus black-box (put/array/
 *          compact on read-only buffers).
 * @run main ReadOnlyBufferExceptionTest
 */

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
        Class<?> c = Class.forName("java.nio.ReadOnlyBufferException");
        check(c.getSuperclass() == java.lang.UnsupportedOperationException.class,
              "extends UnsupportedOperationException");
        check(c.getConstructor() != null, "has no-arg constructor");
        check(java.io.Serializable.class.isAssignableFrom(c), "is Serializable");
        ReadOnlyBufferException e = new ReadOnlyBufferException();
        check(e.getMessage() == null, "default message is null");
    }

    static void testBlackBox() {
        ByteBuffer ro = ByteBuffer.allocate(4).asReadOnlyBuffer();
        check(ro.isReadOnly(), "isReadOnly");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put((byte) 1), "read-only put");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, (byte) 1), "read-only absolute put");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(new byte[] {1, 2}), "read-only bulk put");
        checkThrows(ReadOnlyBufferException.class, () -> ro.compact(), "read-only compact");
        checkThrows(ReadOnlyBufferException.class, () -> ro.array(), "read-only array()");

        // Cursor state is still mutable on a read-only buffer.
        ro.position(2).limit(3).mark();
        check(ro.position() == 2 && ro.limit() == 3, "cursor mutable on read-only buffer");

        // Read-only CharBuffer also rejects writes.
        CharBuffer roc = CharBuffer.wrap("abc").asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, () -> roc.append('x'), "read-only append");

        // Getting from a read-only buffer works and shares storage with the source.
        ByteBuffer src = ByteBuffer.allocate(4).put((byte) 9);
        ByteBuffer roView = src.asReadOnlyBuffer();
        check(roView.get(0) == 9, "read-only view reads");
        src.put(0, (byte) 5);
        check(roView.get(0) == 5, "read-only view sees source mutation");
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
        checkThrows(ReadOnlyBufferException.class, () -> ro.put((short) 1),
                "ShortBuffer read-only relative put");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, (short) 1),
                "ShortBuffer read-only absolute put");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(new short[] {1}, 0, 1),
                "ShortBuffer read-only bulk put");
        checkThrows(ReadOnlyBufferException.class,
                () -> ro.put(ShortBuffer.wrap(new short[] {1})),
                "ShortBuffer read-only put(XBuffer)");
        checkThrows(ReadOnlyBufferException.class, () -> ro.compact(),
                "ShortBuffer read-only compact");
        checkThrows(ReadOnlyBufferException.class, () -> ro.array(),
                "ShortBuffer read-only array()");
        FloatBuffer rof = ByteBuffer.allocate(4).asReadOnlyBuffer().asFloatBuffer();
        check(rof.isReadOnly(), "read-only byte buffer gives a read-only float view");
        checkThrows(ReadOnlyBufferException.class, () -> rof.put(1f),
                "read-only float view put");
        ro.position(1);
        ro.limit(2);
        ro.mark();
        check(ro.position() == 1 && ro.limit() == 2, "cursor stays mutable when read-only");
        check(ro.get(0) == 1, "read-only buffer reads still work");
    }
}
