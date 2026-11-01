/*
 * @test
 * @summary java.nio.charset.UnmappableCharacterException conformance: input length accessor.
 * @run main UnmappableCharacterExceptionTest
 */

import java.nio.charset.CharacterCodingException;
import java.nio.charset.UnmappableCharacterException;

public class UnmappableCharacterExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnmappableCharacterException() {
        UnmappableCharacterException e = new UnmappableCharacterException(6);
        check(e.getInputLength() == 6, "getInputLength");
        check(e instanceof CharacterCodingException, "is CharacterCodingException");
    }

    public static void main(String[] args) {
        testUnmappableCharacterException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
