/*
 * @test
 * @summary java.io.FileNotFoundException API conformance: extends IOException and the () and
 *          (String) constructors.
 * @run main FileNotFoundExceptionTest
 */

import java.io.FileNotFoundException;
import java.io.IOException;

public class FileNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileNotFoundException() {
        FileNotFoundException e = new FileNotFoundException("msg");
        check(e instanceof IOException, "extends IOException");
        check("msg".equals(e.getMessage()), "(String) constructor");
        check(new FileNotFoundException().getMessage() == null, "() constructor null message");
    }

    public static void main(String[] args) {
        testFileNotFoundException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
