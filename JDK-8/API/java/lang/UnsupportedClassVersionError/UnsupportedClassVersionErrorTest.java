/*
 * @test
 * @summary java.lang.UnsupportedClassVersionError API conformance: constructors and inheritance.
 * @run main UnsupportedClassVersionErrorTest
 */

public class UnsupportedClassVersionErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnsupportedClassVersionError() {
        UnsupportedClassVersionError e = new UnsupportedClassVersionError("m");
        check(e instanceof ClassFormatError, "extends ClassFormatError");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testUnsupportedClassVersionError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
