/*
 * @test
 * @summary java.lang.NoClassDefFoundError API conformance: LinkageError hierarchy and the
 *          no-arg/(String) constructors.
 * @run main NoClassDefFoundErrorTest
 */

public class NoClassDefFoundErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        NoClassDefFoundError e = new NoClassDefFoundError();
        check(e instanceof LinkageError, "NCDFE is a LinkageError");
        check(e instanceof Error, "NCDFE is an Error");
    }

    static void testConstructors() {
        NoClassDefFoundError e1 = new NoClassDefFoundError();
        check(e1.getMessage() == null, "no-arg ctor null message");
        NoClassDefFoundError e2 = new NoClassDefFoundError("C");
        check("C".equals(e2.getMessage()), "(String) ctor message");
    }

    public static void main(String[] args) {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
