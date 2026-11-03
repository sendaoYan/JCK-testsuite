/*
 * @test
 * @summary java.lang.instrument.UnmodifiableClassException conformance: white-box structure + black-box behavior.
 * @run main UnmodifiableClassExceptionTest
 */
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Modifier;

public class UnmodifiableClassExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.instrument.UnmodifiableClassException");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(c.getConstructor() != null, "no-arg constructor");
        check(c.getConstructor(String.class) != null, "String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        UnmodifiableClassException e1 = new UnmodifiableClassException();
        check(e1.getMessage() == null, "no-arg message null");
        check(e1 instanceof Exception, "is an Exception");

        UnmodifiableClassException e2 = new UnmodifiableClassException("cannot retransform");
        check("cannot retransform".equals(e2.getMessage()), "String constructor message");
        check(e2.getCause() == null, "cause null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
