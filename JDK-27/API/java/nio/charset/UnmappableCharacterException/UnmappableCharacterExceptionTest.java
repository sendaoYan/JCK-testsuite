/*
 * @test
 * @summary java.nio.charset.UnmappableCharacterException conformance: white-box structure + black-box length/message.
 * @run main UnmappableCharacterExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;


public class UnmappableCharacterExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.UnmappableCharacterException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == CharacterCodingException.class, "superclass is CharacterCodingException");
        check(c.getConstructor(int.class) != null, "int constructor present");
    }

    static void testBlackBox() {
        UnmappableCharacterException e = new UnmappableCharacterException(6);
        check(e.getInputLength() == 6, "getInputLength");
        check("Input length = 6".equals(e.getMessage()), "getMessage");
        check(e instanceof CharacterCodingException, "is CharacterCodingException");
        check(e.getClass().getSuperclass() == CharacterCodingException.class, "superclass is CharacterCodingException");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");
    }

    // Constructor matrix: the int length is echoed verbatim by getMessage() with the exact
    // JDK format "Input length = N" (probed identical on JDK-8 and JDK-27), including
    // zero, negative and extreme values; no validation is performed.
    static void testCtorMatrix() {
        int[] lengths = {0, 1, 6, -1, -2147483648, 2147483647};
        for (int n : lengths) {
            UnmappableCharacterException e = new UnmappableCharacterException(n);
            check(e.getInputLength() == n, "getInputLength() == " + n);
            check(("Input length = " + n).equals(e.getMessage()), "getMessage() for " + n);
        }
        check("java.nio.charset.UnmappableCharacterException: Input length = 2"
                        .equals(new UnmappableCharacterException(2).toString()),
                "toString == className: Input length = N");
        check(new UnmappableCharacterException(3) instanceof IOException, "catchable as IOException");
        check(new UnmappableCharacterException(3) instanceof Serializable, "Serializable via Throwable");
        check(!RuntimeException.class.isInstance(new UnmappableCharacterException(3)),
                "checked exception, not a RuntimeException");
    }

    // Real throw paths from actual encoders in REPORT mode, plus the CoderResult bridge.
    static void testRealTriggers() throws Exception {
        // A single CJK char cannot be mapped by US-ASCII.
        CharsetEncoder enc = StandardCharsets.US_ASCII.newEncoder()
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            enc.encode(java.nio.CharBuffer.wrap("\u4e2d"));
            check(false, "unmappable char must throw");
        } catch (UnmappableCharacterException e) {
            check(e.getInputLength() == 1, "single CJK char length 1");
            check("Input length = 1".equals(e.getMessage()), "trigger message format");
        }
        // The run stops at the first unmappable char; the ASCII prefix is not reported.
        CharsetEncoder enc2 = StandardCharsets.US_ASCII.newEncoder()
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            enc2.encode(java.nio.CharBuffer.wrap("a\u4e2db"));
            check(false, "unmappable char inside text must throw");
        } catch (UnmappableCharacterException e) {
            check(e.getInputLength() == 1, "unmappable inside text length 1");
        }
        // A supplementary character (surrogate pair) is reported with length 2.
        CharsetEncoder enc3 = StandardCharsets.US_ASCII.newEncoder()
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            enc3.encode(java.nio.CharBuffer.wrap("\uD83D\uDE00"));
            check(false, "unmappable emoji must throw");
        } catch (UnmappableCharacterException e) {
            check(e.getInputLength() == 2, "surrogate pair length 2");
        }
        // CoderResult bridge: unmappableForLength(n).throwException() throws UCE(n).
        try {
            CoderResult.unmappableForLength(5).throwException();
            check(false, "throwException must throw");
        } catch (UnmappableCharacterException e) {
            check(e.getInputLength() == 5, "CoderResult bridge length 5");
        }
        // Catchable through the whole checked chain.
        try {
            StandardCharsets.US_ASCII.newEncoder().onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(java.nio.CharBuffer.wrap("\u4e2d"));
            check(false, "must throw");
        } catch (IOException io) {
            check(io instanceof UnmappableCharacterException, "caught as IOException, runtime type UCE");
        }
        // Negative space: REPLACE (the convenience default) never throws UCE.
        check(StandardCharsets.US_ASCII.encode("\u4e2d").remaining() == 1,
                "REPLACE action suppresses the throw");
    }

    // Serialization: fixed serialVersionUID + round trip preserving inputLength.
    static void testSerialization() throws Exception {
        long suid = ObjectStreamClass.lookup(UnmappableCharacterException.class).getSerialVersionUID();
        check(suid == -7026962371537706123L, "serialVersionUID == -7026962371537706123L");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new UnmappableCharacterException(8));
        }
        UnmappableCharacterException rt;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rt = (UnmappableCharacterException) ois.readObject();
        }
        check(rt != null, "round-trip yields UCE");
        check(rt.getInputLength() == 8, "inputLength survives round trip");
        check("Input length = 8".equals(rt.getMessage()), "message survives round trip");
        check(rt instanceof CharacterCodingException, "hierarchy preserved after round trip");
        check(rt instanceof Serializable, "still Serializable after round trip");

        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new UnmappableCharacterException(-4));
        }
        UnmappableCharacterException rtNeg;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rtNeg = (UnmappableCharacterException) ois.readObject();
        }
        check(rtNeg.getInputLength() == -4, "negative length survives round trip");
    }

    // User subclasses may add context on top of the length.
    static final class DetailedUce extends UnmappableCharacterException {
        DetailedUce(int n) { super(n); }
    }

    static void testSubclass() {
        DetailedUce d = new DetailedUce(9);
        check(d.getInputLength() == 9, "subclass ctor delegates length");
        check("Input length = 9".equals(d.getMessage()), "subclass inherits message semantics");
        check(d instanceof UnmappableCharacterException, "subclass instanceof UCE");
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

