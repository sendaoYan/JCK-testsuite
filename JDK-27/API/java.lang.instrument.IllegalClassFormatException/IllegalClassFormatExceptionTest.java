/*
 * @test
 * @summary java.lang.instrument.IllegalClassFormatException conformance: white-box structure + black-box behavior.
 * @run main IllegalClassFormatExceptionTest
 */
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;

public class IllegalClassFormatExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.instrument.IllegalClassFormatException");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(c.getConstructor() != null, "no-arg constructor");
        check(c.getConstructor(String.class) != null, "String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        IllegalClassFormatException e1 = new IllegalClassFormatException();
        check(e1.getMessage() == null, "no-arg message null");
        check(e1 instanceof Exception, "is an Exception");

        IllegalClassFormatException e2 = new IllegalClassFormatException("bad class file");
        check("bad class file".equals(e2.getMessage()), "String constructor message");
        check(e2.getCause() == null, "cause null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
