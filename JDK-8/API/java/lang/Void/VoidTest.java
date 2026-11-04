/*
 * @test
 * @summary java.lang.Void API conformance: Void.TYPE is the primitive void class, with the
 *          name "void" and the primitive flag set.
 * @run main VoidTest
 */

public class VoidTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testType() {
        check(Void.TYPE == void.class, "Void.TYPE == void.class");
        check(Void.TYPE.getName().equals("void"), "Void.TYPE name is \"void\"");
        check(Void.TYPE.isPrimitive(), "Void.TYPE is primitive");
        check(Void.class != Void.TYPE, "Void.class (wrapper) != Void.TYPE (primitive)");
        check(Void.class.getName().equals("java.lang.Void"), "Void.class name is java.lang.Void");
    }

    public static void main(String[] args) {
        testType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
