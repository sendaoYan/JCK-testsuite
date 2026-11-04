/*
 * @test
 * @summary com.sun.media.sound.InvalidDataException conformance: IOException message semantics.
 * @run main InvalidDataExceptionTest
 */

import com.sun.media.sound.InvalidDataException;
import java.io.IOException;

public class InvalidDataExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        InvalidDataException e0 = new InvalidDataException();
        check(e0 instanceof IOException, "extends IOException");
        check("Invalid Data!".equals(e0.getMessage()), "no-arg default message");

        InvalidDataException e1 = new InvalidDataException("boom");
        check("boom".equals(e1.getMessage()), "string message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
