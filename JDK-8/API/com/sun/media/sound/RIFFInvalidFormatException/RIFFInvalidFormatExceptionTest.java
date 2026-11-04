/*
 * @test
 * @summary com.sun.media.sound.RIFFInvalidFormatException conformance: final, extends InvalidFormatException.
 * @run main RIFFInvalidFormatExceptionTest
 */

import com.sun.media.sound.RIFFInvalidFormatException;
import com.sun.media.sound.InvalidFormatException;
import java.lang.reflect.Modifier;

public class RIFFInvalidFormatExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        RIFFInvalidFormatException e0 = new RIFFInvalidFormatException();
        check(e0 instanceof InvalidFormatException, "extends InvalidFormatException");
        check(e0 instanceof java.io.IOException, "is IOException");
        check(Modifier.isFinal(e0.getClass().getModifiers()), "final");
        check("Invalid format!".equals(e0.getMessage()), "no-arg default message");

        RIFFInvalidFormatException e1 = new RIFFInvalidFormatException("boom");
        check("boom".equals(e1.getMessage()), "string message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
