/*
 * @test
 * @summary com.sun.media.sound.RIFFInvalidDataException conformance: final, extends InvalidDataException.
 * @run main RIFFInvalidDataExceptionTest
 */

import com.sun.media.sound.RIFFInvalidDataException;
import com.sun.media.sound.InvalidDataException;
import java.lang.reflect.Modifier;

public class RIFFInvalidDataExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        RIFFInvalidDataException e0 = new RIFFInvalidDataException();
        check(e0 instanceof InvalidDataException, "extends InvalidDataException");
        check(e0 instanceof java.io.IOException, "is IOException");
        check(Modifier.isFinal(e0.getClass().getModifiers()), "final");
        check("Invalid Data!".equals(e0.getMessage()), "no-arg default message");

        RIFFInvalidDataException e1 = new RIFFInvalidDataException("boom");
        check("boom".equals(e1.getMessage()), "string message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
