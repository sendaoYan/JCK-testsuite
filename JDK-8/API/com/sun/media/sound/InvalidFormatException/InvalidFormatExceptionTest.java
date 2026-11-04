/*
 * @test
 * @summary com.sun.media.sound.InvalidFormatException conformance: extends InvalidDataException.
 * @run main InvalidFormatExceptionTest
 */

import com.sun.media.sound.InvalidFormatException;
import com.sun.media.sound.InvalidDataException;

public class InvalidFormatExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        InvalidFormatException e0 = new InvalidFormatException();
        check(e0 instanceof InvalidDataException, "extends InvalidDataException");
        check(e0 instanceof java.io.IOException, "is IOException");
        check("Invalid format!".equals(e0.getMessage()), "no-arg default message");

        InvalidFormatException e1 = new InvalidFormatException("boom");
        check("boom".equals(e1.getMessage()), "string message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
