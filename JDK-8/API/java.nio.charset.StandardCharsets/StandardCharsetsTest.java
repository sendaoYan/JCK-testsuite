/*
 * @test
 * @summary java.nio.charset.StandardCharsets API conformance: well-known charset names
 *          and singletons.
 * @run main StandardCharsetsTest
 */

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StandardCharsetsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStandardCharsets() {
        check("UTF-8".equals(StandardCharsets.UTF_8.name()), "UTF-8 name");
        check("US-ASCII".equals(StandardCharsets.US_ASCII.name()), "US-ASCII name");
        check("ISO-8859-1".equals(StandardCharsets.ISO_8859_1.name()), "ISO-8859-1 name");
        check("UTF-16".equals(StandardCharsets.UTF_16.name()), "UTF-16 name");
        check(StandardCharsets.UTF_8 == Charset.forName("UTF-8"), "UTF-8 singleton");
    }

    public static void main(String[] args) {
        testStandardCharsets();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
