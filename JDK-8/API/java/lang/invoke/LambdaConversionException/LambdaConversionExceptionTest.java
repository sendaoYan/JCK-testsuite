/*
 * @test
 * @summary java.lang.invoke.LambdaConversionException conformance: hierarchy and ctors.
 * @run main LambdaConversionExceptionTest
 */

import java.lang.invoke.LambdaConversionException;

public class LambdaConversionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLambdaConversionException() {
        check(Exception.class.isAssignableFrom(LambdaConversionException.class),
              "extends Exception");
        check(new LambdaConversionException().getMessage() == null, "()");
        check("m".equals(new LambdaConversionException("m").getMessage()), "(String)");
        Throwable cause = new Throwable();
        check(new LambdaConversionException("m", cause).getCause() == cause, "(String,Throwable)");
        check(new LambdaConversionException(cause).getCause() == cause, "(Throwable)");
        check(new LambdaConversionException("m", cause, true, false).getCause() == cause,
              "(String,Throwable,boolean,boolean)");
    }

    public static void main(String[] args) {
        testLambdaConversionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
