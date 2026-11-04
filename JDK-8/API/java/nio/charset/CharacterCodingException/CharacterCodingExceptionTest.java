/*
 * @test
 * @summary java.nio.charset.CharacterCodingException conformance: white-box structure + black-box ctor and real throw path.
 * @run main CharacterCodingExceptionTest
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
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;

public class CharacterCodingExceptionTest {
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
        Class<?> c = Class.forName("java.nio.charset.CharacterCodingException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == IOException.class, "superclass is IOException");
        check(c.getConstructor() != null, "no-arg constructor present");
    }

    static void testBlackBox() throws Exception {
        CharacterCodingException e = new CharacterCodingException();
        check(e instanceof IOException, "extends IOException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        // Real throw path: an unmappable character via CoderResult.throwException().
        CharsetEncoder enc = StandardCharsets.US_ASCII.newEncoder();
        enc.onUnmappableCharacter(CodingErrorAction.REPORT).onMalformedInput(CodingErrorAction.REPORT);
        enc.reset();
        CoderResult cr = enc.encode(CharBuffer.wrap("€"), ByteBuffer.allocate(8), true);
        check(cr.isUnmappable(), "euro sign is unmappable in US-ASCII");
        try {
            cr.throwException();
            check(false, "throwException should throw");
        } catch (CharacterCodingException thrown) {
            check(thrown instanceof UnmappableCharacterException, "throwException yields UnmappableCharacterException");
            check(thrown instanceof IOException, "CharacterCodingException is an IOException");
        }
    }

    // Family hierarchy: CCE is the common checked parent of the malformed/unmappable pair.
    static void testFamilyHierarchy() {
        check(new MalformedInputException(1) instanceof CharacterCodingException, "MIE is a CCE");
        check(new UnmappableCharacterException(1) instanceof CharacterCodingException, "UCE is a CCE");
        check(!(new CharacterCodingException() instanceof MalformedInputException), "plain CCE is not a MIE");
        check(!(new CharacterCodingException() instanceof UnmappableCharacterException), "plain CCE is not a UCE");
        check(IOException.class.isAssignableFrom(CharacterCodingException.class), "IOException assignable from CCE");
        check(CharacterCodingException.class.getSuperclass() == IOException.class, "direct superclass IOException");
        check(Exception.class.isAssignableFrom(CharacterCodingException.class), "checked: assignable from Exception");
        check(new CharacterCodingException() instanceof Serializable, "Serializable via Throwable");
    }

    // Constructor/Throwable-state contract: only a no-arg ctor exists, so the message is
    // fixed null and a cause can only be attached afterwards via initCause.
    static void testCtorAndThrowableState() {
        CharacterCodingException e = new CharacterCodingException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getLocalizedMessage() == null, "getLocalizedMessage null");
        check("java.nio.charset.CharacterCodingException".equals(e.toString()), "toString has no message suffix");
        IllegalStateException cause = new IllegalStateException("later-attached");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "initCause then getCause");
        check(e.getMessage() == null, "initCause does not touch message");
        check("java.nio.charset.CharacterCodingException".equals(e.toString()), "toString still message-free after initCause");
        try {
            e.initCause(new IllegalStateException("second"));
            check(false, "double initCause must throw");
        } catch (IllegalStateException expected) {
            check(true, "double initCause throws IllegalStateException");
        }
        e.addSuppressed(new IllegalStateException("supp"));
        check(e.getSuppressed().length == 1, "addSuppressed/getSuppressed honored");
    }

    // Real throw paths: decode REPORT surfaces it as MalformedInputException,
    // encode REPORT as UnmappableCharacterException; both are caught as CCE / IOException.
    static void testRealThrowPaths() throws Exception {
        CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT);
        try {
            dec.decode(ByteBuffer.wrap(new byte[]{(byte) 0xC3}));
            check(false, "malformed decode must throw");
        } catch (CharacterCodingException cce) {
            check(cce instanceof MalformedInputException, "runtime type is MalformedInputException");
            check(((MalformedInputException) cce).getInputLength() == 1, "wrapped MIE length 1");
            check(cce instanceof IOException, "catchable as IOException");
        }
        try {
            StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(new byte[]{(byte) 0xC3}));
            check(false, "malformed decode must throw (IOException catch)");
        } catch (IOException io) {
            check(io instanceof CharacterCodingException, "catchable via IOException");
        }
        try {
            StandardCharsets.US_ASCII.newEncoder().onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap("\u4e2d"));
            check(false, "unmappable encode must throw");
        } catch (CharacterCodingException cce) {
            check(cce instanceof UnmappableCharacterException, "encode path yields UCE");
            check(((UnmappableCharacterException) cce).getInputLength() == 1, "wrapped UCE length 1");
        }
        // Negative space: the Charset.encode/decode convenience entry points use REPLACE
        // by default, so they never surface a CharacterCodingException.
        check(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(new byte[]{(byte) 0xC3})).length() == 1,
                "Charset.decode convenience REPLACEs malformed input");
        check(StandardCharsets.US_ASCII.encode("\u4e2d").remaining() == 1,
                "Charset.encode convenience REPLACEs unmappable char");
    }

    // Serialization: fixed serialVersionUID (probed identical on JDK-8 and JDK-27) + round trip.
    static void testSerialization() throws Exception {
        long suid = ObjectStreamClass.lookup(CharacterCodingException.class).getSerialVersionUID();
        check(suid == 8421532232154627783L, "serialVersionUID == 8421532232154627783L");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new CharacterCodingException());
        }
        CharacterCodingException rt;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rt = (CharacterCodingException) ois.readObject();
        }
        check(rt != null, "round-trip yields CCE instance");
        check(rt.getMessage() == null, "message null after round trip");
        check(rt.getCause() == null, "cause null after round trip");
        check(rt instanceof IOException, "hierarchy preserved after round trip");
        check(rt instanceof Serializable, "still Serializable after round trip");

        CharacterCodingException withCause = new CharacterCodingException();
        withCause.initCause(new IllegalStateException("serial-cause"));
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(withCause);
        }
        CharacterCodingException rt2;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rt2 = (CharacterCodingException) ois.readObject();
        }
        check(rt2.getCause() instanceof IllegalStateException, "attached cause survives round trip");
        check("serial-cause".equals(rt2.getCause().getMessage()), "cause message survives round trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFamilyHierarchy();
        testCtorAndThrowableState();
        testRealThrowPaths();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
