/*
 * @test
 * @summary java.nio.charset.CharacterCodingException conformance: white-box structure + black-box ctor and real throw path.
 * @run main CharacterCodingExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
