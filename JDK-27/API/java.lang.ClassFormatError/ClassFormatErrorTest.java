/*
 * @test
 * @summary java.lang.ClassFormatError API conformance: constructors and inheritance.
 * @run main ClassFormatErrorTest
 */

public class ClassFormatErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClassFormatError() {
        ClassFormatError e = new ClassFormatError("m");
        check(e instanceof LinkageError, "extends LinkageError");
        check(e instanceof Error, "extends Error");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testClassFormatError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
