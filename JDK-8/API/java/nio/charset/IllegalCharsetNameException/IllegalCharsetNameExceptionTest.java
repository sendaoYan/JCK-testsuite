/*
 * @test
 * @summary java.nio.charset.IllegalCharsetNameException conformance: name accessor and forName.
 * @run main IllegalCharsetNameExceptionTest
 */

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

public class IllegalCharsetNameExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalCharsetNameException() {
        IllegalCharsetNameException e = new IllegalCharsetNameException("bad name!");
        check("bad name!".equals(e.getCharsetName()), "getCharsetName");
        check(e instanceof IllegalArgumentException, "is IllegalArgumentException");
        try {
            Charset.forName("bad name!");
            check(false, "forName should throw");
        } catch (IllegalCharsetNameException expected) {
            check("bad name!".equals(expected.getCharsetName()), "forName throws");
        }
    }

    public static void main(String[] args) {
        testIllegalCharsetNameException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
