/*
 * @test
 * @summary java.lang.instrument.UnmodifiableModuleException conformance: white-box structure + black-box behavior.
 * @run main UnmodifiableModuleExceptionTest
 */
import java.lang.instrument.UnmodifiableModuleException;
import java.lang.reflect.Modifier;

public class UnmodifiableModuleExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.instrument.UnmodifiableModuleException");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(c.getConstructor() != null, "no-arg constructor");
        check(c.getConstructor(String.class) != null, "String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        UnmodifiableModuleException e1 = new UnmodifiableModuleException();
        check(e1.getMessage() == null, "no-arg message null");
        check(e1 instanceof RuntimeException, "is a RuntimeException");

        UnmodifiableModuleException e2 = new UnmodifiableModuleException("module not modifiable");
        check("module not modifiable".equals(e2.getMessage()), "String constructor message");
        check(e2.getCause() == null, "cause null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
