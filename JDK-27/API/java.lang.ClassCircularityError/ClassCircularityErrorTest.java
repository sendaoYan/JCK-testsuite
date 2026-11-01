/*
 * @test
 * @summary java.lang.ClassCircularityError API conformance: constructors and inheritance.
 * @run main ClassCircularityErrorTest
 */

public class ClassCircularityErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClassCircularityError() {
        ClassCircularityError e = new ClassCircularityError("m");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testClassCircularityError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
