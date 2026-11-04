/*
 * @test
 * @summary java.util.MissingResourceException API conformance: getClassName/getKey.
 * @run main MissingResourceExceptionTest
 */

import java.util.MissingResourceException;

public class MissingResourceExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMissingResourceException() {
        MissingResourceException e = new MissingResourceException("m", "ClassName", "key");
        check(e instanceof RuntimeException, "extends RuntimeException");
        check("ClassName".equals(e.getClassName()), "getClassName");
        check("key".equals(e.getKey()), "getKey");
    }

    public static void main(String[] args) {
        testMissingResourceException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
