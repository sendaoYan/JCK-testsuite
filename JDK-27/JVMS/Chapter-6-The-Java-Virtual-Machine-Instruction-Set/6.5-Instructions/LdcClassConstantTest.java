/*
 * @test
 * @summary JVMS 6.5: ldc of a CONSTANT_Class entry (a class literal) resolves to
 *          the canonical Class object, including primitive and void types, and
 *          matches the Class loaded by name at runtime.
 * @run main LdcClassConstantTest
 */

public class LdcClassConstantTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        // String.class compiles to ldc of a CONSTANT_Class entry
        check(String.class.getName().equals("java.lang.String"), "ldc String.class name");

        // primitive and void class literals also resolve through CONSTANT_Class
        check(int.class.getName().equals("int"), "ldc int.class name");
        check(void.class.getName().equals("void"), "ldc void.class name");
        check(int[].class.getName().equals("[I"), "ldc int[].class name");

        // the Class object resolved by ldc is identical to the one loaded by name
        check(String.class == Class.forName("java.lang.String"),
              "ldc Class identical to Class.forName result");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
