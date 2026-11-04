/*
 * @test
 * @summary java.nio.charset.CoderMalfunctionError conformance: white-box structure + black-box cause chain.
 * @run main CoderMalfunctionErrorTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CoderResult;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class CoderMalfunctionErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.CoderMalfunctionError");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == Error.class, "superclass is Error");
        check(c.getConstructor(Exception.class) != null, "Exception constructor present");
    }

    static void testBlackBox() {
        Exception cause = new Exception("cause");
        CoderMalfunctionError e = new CoderMalfunctionError(cause);
        check(e instanceof Error, "is Error");
        check(e.getCause() == cause, "getCause");
        check(e.getMessage().equals(cause.toString()), "message is cause.toString()");
        check(e.getStackTrace().length > 0, "stack trace captured");
    }

    // A decoder whose decodeLoop misbehaves on demand; used to drive the
    // CharsetDecoder.decode wrapping path that produces CoderMalfunctionError.
    static final class BoomDecoder extends CharsetDecoder {
        private final RuntimeException boom;
        BoomDecoder(RuntimeException boom) {
            super(StandardCharsets.ISO_8859_1, 1.0f, 1.0f);
            this.boom = boom;
        }
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            throw boom;
        }
    }

    // An encoder whose encodeLoop misbehaves on demand.
    static final class BoomEncoder extends CharsetEncoder {
        private final RuntimeException boom;
        BoomEncoder(RuntimeException boom) {
            super(StandardCharsets.ISO_8859_1, 1.0f, 1.0f, new byte[]{0x3f});
            this.boom = boom;
        }
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            throw boom;
        }
    }

    static void testCtorMatrix() {
        Exception cause = new Exception("boom-cause");
        CoderMalfunctionError e = new CoderMalfunctionError(cause);
        check(e.getCause() == cause, "cause identity preserved");
        check(cause.toString().equals(e.getMessage()), "message == cause.toString()");
        check(("java.nio.charset.CoderMalfunctionError: " + cause).equals(e.toString()),
                "toString == className: cause.toString()");
        check(e instanceof Error, "is Error");
        check(!Exception.class.isInstance(e), "an Error, never an Exception");

        CoderMalfunctionError nullCause = new CoderMalfunctionError(null);
        check(nullCause.getCause() == null, "null cause permitted, getCause null");
        check(nullCause.getMessage() == null,
                "null cause message is null (Throwable(Throwable) yields null, not \"null\")");
        check("java.nio.charset.CoderMalfunctionError".equals(nullCause.toString()),
                "toString without message suffix for null cause");

        // Cause is fixed by the constructor: a later initCause is rejected.
        try {
            e.initCause(new Exception("second"));
            check(false, "re-initCause must throw");
        } catch (IllegalStateException expected) {
            check(true, "re-initCause throws IllegalStateException");
        }
    }

    // Real contract-level trigger: decodeLoop throwing BufferOverflowException is wrapped
    // into CoderMalfunctionError with the original exception as cause.
    static void testRealTriggerDecodeLoop() throws Exception {
        BufferOverflowException boom = new BufferOverflowException();
        BoomDecoder d = new BoomDecoder(boom);
        try {
            d.decode(ByteBuffer.wrap(new byte[]{0x41, 0x42}), CharBuffer.allocate(10), true);
            check(false, "decodeLoop BufferOverflowException must surface as error");
        } catch (CoderMalfunctionError err) {
            check(err.getCause() == boom, "decoder path preserves cause identity");
            check(err instanceof Error, "wrapped as Error");
        }
        // Convenience decode(ByteBuffer) entry point surfaces the same wrap.
        BoomDecoder d2 = new BoomDecoder(new BufferUnderflowException());
        try {
            d2.decode(ByteBuffer.wrap(new byte[]{0x41}));
            check(false, "convenience decode must surface the error too");
        } catch (CoderMalfunctionError err) {
            check(err.getCause() instanceof BufferUnderflowException,
                    "BufferUnderflowException also wrapped via convenience path");
        }
    }

    // Encoder-side wrap: encodeLoop throwing BufferOverflowException is wrapped as well.
    static void testRealTriggerEncodeLoop() throws Exception {
        BufferOverflowException boom = new BufferOverflowException();
        BoomEncoder en = new BoomEncoder(boom);
        try {
            en.encode(CharBuffer.wrap("AB"), ByteBuffer.allocate(10), true);
            check(false, "encodeLoop BufferOverflowException must surface as error");
        } catch (CoderMalfunctionError err) {
            check(err.getCause() == boom, "encoder path preserves cause identity");
        }
    }

    // JDK-27 specific wrap scope: CharsetDecoder.decode / CharsetEncoder.encode catch
    // RuntimeException from decodeLoop/encodeLoop (jdk27 Charset-X-Coder.java.template,
    // catch (RuntimeException x) clause), so any unchecked exception is wrapped.
    static void testWrapScopeJdk27() throws Exception {
        ArithmeticException boom = new ArithmeticException("kaboom");
        BoomDecoder d = new BoomDecoder(boom);
        try {
            d.decode(ByteBuffer.wrap(new byte[]{0x41}), CharBuffer.allocate(10), true);
            check(false, "decodeLoop exception must surface");
        } catch (CoderMalfunctionError err) {
            check(err.getCause() == boom, "JDK-27 wraps arbitrary RuntimeException, cause identity preserved");
        }
    }

    static void testSerialization() throws Exception {
        long suid = ObjectStreamClass.lookup(CoderMalfunctionError.class).getSerialVersionUID();
        check(suid == -1151412348057794301L, "serialVersionUID == -1151412348057794301L");
        CoderMalfunctionError orig = new CoderMalfunctionError(new Exception("serial-cause"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(orig);
        }
        CoderMalfunctionError rt;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rt = (CoderMalfunctionError) ois.readObject();
        }
        check(rt != null, "round-trip yields CoderMalfunctionError");
        check(rt instanceof Error, "still an Error after round trip");
        check(rt.getCause() instanceof Exception, "cause survives round trip");
        check("serial-cause".equals(rt.getCause().getMessage()), "cause message survives round trip");
        check(rt.getMessage().equals(rt.getCause().toString()), "message recomputed as cause.toString() after round trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorMatrix();
        testRealTriggerDecodeLoop();
        testRealTriggerEncodeLoop();
        testWrapScopeJdk27();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
