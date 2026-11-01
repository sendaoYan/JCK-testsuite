/*
 * @test
 * @summary java.io.InvalidClassException API conformance: getClassname and inheritance.
 * @run main InvalidClassExceptionTest
 */

import java.io.InvalidClassException;
import java.io.ObjectStreamException;

public class InvalidClassExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInvalidClassException() {
        InvalidClassException e = new InvalidClassException("cn", "reason");
        check(e instanceof ObjectStreamException, "extends ObjectStreamException");
        check("cn".equals(e.classname), "classname field");
        check("cn; reason".equals(e.getMessage()), "message format");
        check(new InvalidClassException("only reason").classname == null, "no-classname ctor leaves classname null");
    }

    public static void main(String[] args) {
        testInvalidClassException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
