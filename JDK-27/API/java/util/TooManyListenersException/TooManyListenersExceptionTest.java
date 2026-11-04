/*
 * @test
 * @summary java.util.TooManyListenersException conformance: white-box structure + black-box behavior.
 * @run main TooManyListenersExceptionTest
 */

import java.util.TooManyListenersException;

public class TooManyListenersExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.TooManyListenersException");
        check(c.getSuperclass() == Exception.class, "extends Exception (checked)");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredConstructors().length == 2, "two constructors");
    }

    static void testBlackBox() {
        check(new TooManyListenersException().getMessage() == null, "no-arg message null");
        check("too many".equals(new TooManyListenersException("too many").getMessage()),
                "String ctor message");
        boolean caught = false;
        try {
            throw new TooManyListenersException("m");
        } catch (TooManyListenersException e) {
            caught = true;
        }
        check(caught, "catchable as checked exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
