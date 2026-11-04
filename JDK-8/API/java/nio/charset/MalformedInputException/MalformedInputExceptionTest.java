/*
 * @test
 * @summary java.nio.charset.MalformedInputException conformance: white-box structure + black-box length/message.
 * @run main MalformedInputExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;

public class MalformedInputExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.MalformedInputException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == CharacterCodingException.class, "superclass is CharacterCodingException");
        check(c.getConstructor(int.class) != null, "int constructor present");
    }

    static void testBlackBox() {
        MalformedInputException e = new MalformedInputException(5);
        check(e.getInputLength() == 5, "getInputLength");
        check("Input length = 5".equals(e.getMessage()), "getMessage");
        check(e instanceof CharacterCodingException, "is CharacterCodingException");
        check(e.getClass().getSuperclass() == CharacterCodingException.class, "superclass is CharacterCodingException");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");
    }

    // Constructor matrix: the int length is echoed verbatim by getMessage() with the exact
    // JDK format "Input length = N" (probed identical on JDK-8 and JDK-27), including
    // zero, negative and extreme values; no validation is performed.
    static void testCtorMatrix() {
        int[] lengths = {0, 1, 5, -1, -2147483648, 2147483647};
        for (int n : lengths) {
            MalformedInputException e = new MalformedInputException(n);
            check(e.getInputLength() == n, "getInputLength() == " + n);
            check(("Input length = " + n).equals(e.getMessage()), "getMessage() for " + n);
        }
        check("java.nio.charset.MalformedInputException: Input length = 2"
                        .equals(new MalformedInputException(2).toString()),
                "toString == className: Input length = N");
        check(new MalformedInputException(3) instanceof IOException, "catchable as IOException");
        check(new MalformedInputException(3) instanceof Serializable, "Serializable via Throwable");
        check(!RuntimeException.class.isInstance(new MalformedInputException(3)),
                "checked exception, not a RuntimeException");
    }

    // Real throw paths from actual decoders in REPORT mode, plus the CoderResult bridge.
    static void testRealTriggers() throws Exception {
        // Truncated UTF-8 two-byte sequence: single stray continuation byte.
        CharsetDecoder d = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT);
        try {
            d.decode(ByteBuffer.wrap(new byte[]{(byte) 0xC3}));
            check(false, "stray continuation must throw");
        } catch (MalformedInputException e) {
            check(e.getInputLength() == 1, "stray continuation length 1");
            check("Input length = 1".equals(e.getMessage()), "trigger message format");
        }
        // Truncated UTF-8 three-byte sequence reports the length of the whole broken run.
        CharsetDecoder d2 = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT);
        try {
            d2.decode(ByteBuffer.wrap(new byte[]{(byte) 0xE2, (byte) 0x82}));
            check(false, "truncated 3-byte sequence must throw");
        } catch (MalformedInputException e) {
            check(e.getInputLength() == 2, "truncated 3-byte sequence length 2");
        }
        // UTF-16 decoder on odd-length input.
        CharsetDecoder d16 = StandardCharsets.UTF_16.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT);
        try {
            d16.decode(ByteBuffer.wrap(new byte[]{0x00, 0x41, 0x00}));
            check(false, "odd UTF-16 input must throw");
        } catch (MalformedInputException e) {
            check(e.getInputLength() == 1, "odd UTF-16 trailing byte length 1");
        }
        // Encoder side: a lone surrogate fed to the UTF-8 encoder is malformed input too.
        try {
            StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .encode(java.nio.CharBuffer.wrap("\uD800"));
            check(false, "lone surrogate must throw");
        } catch (MalformedInputException e) {
            check(e.getInputLength() == 1, "lone surrogate length 1");
        }
        // CoderResult bridge: malformedForLength(n).throwException() throws MIE(n).
        try {
            CoderResult.malformedForLength(4).throwException();
            check(false, "throwException must throw");
        } catch (MalformedInputException e) {
            check(e.getInputLength() == 4, "CoderResult bridge length 4");
        }
        // Catchable through the whole checked chain.
        try {
            StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(new byte[]{(byte) 0xC3}));
            check(false, "must throw");
        } catch (IOException io) {
            check(io instanceof MalformedInputException, "caught as IOException, runtime type MIE");
        }
        // Negative space: REPLACE (the convenience default) never throws MIE.
        check(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(new byte[]{(byte) 0xC3})).length() == 1,
                "REPLACE action suppresses the throw");
    }

    // Serialization: fixed serialVersionUID + round trip preserving inputLength.
    static void testSerialization() throws Exception {
        long suid = ObjectStreamClass.lookup(MalformedInputException.class).getSerialVersionUID();
        check(suid == -3438823399834806194L, "serialVersionUID == -3438823399834806194L");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new MalformedInputException(7));
        }
        MalformedInputException rt;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rt = (MalformedInputException) ois.readObject();
        }
        check(rt != null, "round-trip yields MIE");
        check(rt.getInputLength() == 7, "inputLength survives round trip");
        check("Input length = 7".equals(rt.getMessage()), "message survives round trip");
        check(rt instanceof CharacterCodingException, "hierarchy preserved after round trip");
        check(rt instanceof Serializable, "still Serializable after round trip");

        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new MalformedInputException(-3));
        }
        MalformedInputException rtNeg;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rtNeg = (MalformedInputException) ois.readObject();
        }
        check(rtNeg.getInputLength() == -3, "negative length survives round trip");
    }

    // User subclasses may add context on top of the length.
    static final class DetailedMie extends MalformedInputException {
        DetailedMie(int n) { super(n); }
    }

    static void testSubclass() {
        DetailedMie d = new DetailedMie(9);
        check(d.getInputLength() == 9, "subclass ctor delegates length");
        check("Input length = 9".equals(d.getMessage()), "subclass inherits message semantics");
        check(d instanceof MalformedInputException, "subclass instanceof MIE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorMatrix();
        testRealTriggers();
        testSerialization();
        testSubclass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
