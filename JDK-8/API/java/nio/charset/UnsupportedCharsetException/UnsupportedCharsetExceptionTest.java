/*
 * @test
 * @summary java.nio.charset.UnsupportedCharsetException conformance: name accessor and forName.
 * @run main UnsupportedCharsetExceptionTest
 */

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class UnsupportedCharsetExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnsupportedCharsetException() {
        UnsupportedCharsetException e = new UnsupportedCharsetException("no-such-charset");
        check("no-such-charset".equals(e.getCharsetName()), "getCharsetName");
        check(e instanceof IllegalArgumentException, "is IllegalArgumentException");
        try {
            Charset.forName("definitely-not-a-real-charset");
            check(false, "forName should throw");
        } catch (UnsupportedCharsetException expected) {
            check("definitely-not-a-real-charset".equals(expected.getCharsetName()), "forName throws");
        }
    }

    public static void main(String[] args) {
        testUnsupportedCharsetException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
