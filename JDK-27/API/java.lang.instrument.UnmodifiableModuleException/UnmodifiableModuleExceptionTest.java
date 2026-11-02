/*
 * @test
 * @summary java.lang.instrument.UnmodifiableModuleException conformance: exception hierarchy and constructors.
 * @run main UnmodifiableModuleExceptionTest
 */
import java.lang.instrument.UnmodifiableModuleException;
public class UnmodifiableModuleExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(java.lang.RuntimeException.class.isAssignableFrom(UnmodifiableModuleException.class), "extends RuntimeException");
        UnmodifiableModuleException e = new UnmodifiableModuleException("msg"); check("msg".equals(e.getMessage()), "message");
        check(new UnmodifiableModuleException().getMessage() == null, "no-arg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
