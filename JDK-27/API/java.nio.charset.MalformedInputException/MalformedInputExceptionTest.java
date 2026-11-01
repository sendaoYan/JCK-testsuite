/*
 * @test
 * @summary java.nio.charset.MalformedInputException conformance: input length accessor.
 * @run main MalformedInputExceptionTest
 */

import java.nio.charset.CharacterCodingException;
import java.nio.charset.MalformedInputException;

public class MalformedInputExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMalformedInputException() {
        MalformedInputException e = new MalformedInputException(5);
        check(e.getInputLength() == 5, "getInputLength");
        check(e instanceof CharacterCodingException, "is CharacterCodingException");
    }

    public static void main(String[] args) {
        testMalformedInputException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
