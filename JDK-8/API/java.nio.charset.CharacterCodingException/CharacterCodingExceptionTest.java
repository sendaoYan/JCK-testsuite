/*
 * @test
 * @summary java.nio.charset.CharacterCodingException conformance: hierarchy and no-arg ctor.
 * @run main CharacterCodingExceptionTest
 */

import java.io.IOException;
import java.nio.charset.CharacterCodingException;

public class CharacterCodingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharacterCodingException() {
        CharacterCodingException e = new CharacterCodingException();
        check(e instanceof IOException, "extends IOException");
        check(e.getMessage() == null, "no message");
    }

    public static void main(String[] args) {
        testCharacterCodingException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
