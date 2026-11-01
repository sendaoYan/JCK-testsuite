/*
 * @test
 * @summary java.io.CharConversionException API conformance: constructors and inheritance.
 * @run main CharConversionExceptionTest
 */

import java.io.CharConversionException;
import java.io.IOException;

public class CharConversionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharConversionException() {
        CharConversionException e = new CharConversionException("m");
        check(e instanceof IOException, "extends IOException");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new CharConversionException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testCharConversionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
