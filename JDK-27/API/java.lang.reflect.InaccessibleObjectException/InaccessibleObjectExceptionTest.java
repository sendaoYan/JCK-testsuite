/*
 * @test
 * @summary java.lang.reflect.InaccessibleObjectException conformance: exception hierarchy and constructors.
 * @run main InaccessibleObjectExceptionTest
 */
import java.lang.reflect.InaccessibleObjectException;
public class InaccessibleObjectExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(java.lang.RuntimeException.class.isAssignableFrom(InaccessibleObjectException.class), "extends RuntimeException");
        InaccessibleObjectException e = new InaccessibleObjectException("msg"); check("msg".equals(e.getMessage()), "message");
        check(new InaccessibleObjectException().getMessage() == null, "no-arg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
