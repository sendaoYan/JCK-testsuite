/*
 * @test
 * @summary java.io.UnsupportedEncodingException API conformance: constructors and inheritance.
 * @run main UnsupportedEncodingExceptionTest
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class UnsupportedEncodingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnsupportedEncodingException() {
        UnsupportedEncodingException e = new UnsupportedEncodingException("msg");
        check(e instanceof IOException, "extends IOException");
        check("msg".equals(e.getMessage()), "message from (String) ctor");
        check(new UnsupportedEncodingException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testUnsupportedEncodingException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
