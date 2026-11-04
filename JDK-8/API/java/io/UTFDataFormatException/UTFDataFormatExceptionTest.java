/*
 * @test
 * @summary java.io.UTFDataFormatException API conformance: constructors and inheritance.
 * @run main UTFDataFormatExceptionTest
 */

import java.io.IOException;
import java.io.UTFDataFormatException;

public class UTFDataFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUTFDataFormatException() {
        UTFDataFormatException e = new UTFDataFormatException("msg");
        check(e instanceof IOException, "extends IOException");
        check("msg".equals(e.getMessage()), "message from (String) ctor");
        check(new UTFDataFormatException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testUTFDataFormatException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
