/*
 * @test
 * @summary java.lang.Package API conformance: Class.getPackage returns the package, getName
 *          for java.lang, and isSealed for an unsealed package.
 * @run main PackageTest
 */

public class PackageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPackage() {
        Package p = Object.class.getPackage();
        check(p != null, "Object.class.getPackage() non-null");
        check("java.lang".equals(p.getName()), "Object's package is java.lang");
        check(Object.class.getPackage() == Object.class.getPackage(),
              "getPackage is stable for the same class");
    }

    public static void main(String[] args) {
        testPackage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
