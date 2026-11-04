/*
 * @test
 * @summary java.lang.invoke.WrongMethodTypeException conformance: hierarchy and ctors.
 * @run main WrongMethodTypeExceptionTest
 */

import java.lang.invoke.WrongMethodTypeException;

public class WrongMethodTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWrongMethodTypeException() {
        check(RuntimeException.class.isAssignableFrom(WrongMethodTypeException.class),
              "extends RuntimeException");
        check(new WrongMethodTypeException().getMessage() == null, "()");
        check("wrong".equals(new WrongMethodTypeException("wrong").getMessage()), "(String)");
    }

    public static void main(String[] args) {
        testWrongMethodTypeException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
