/*
 * @test
 * @summary JLS 7.4.2 (Java SE 8): a compilation unit with no package declaration
 *          is part of an unnamed package; such a type has no Package object and
 *          its fully qualified name is its simple name.
 * @run main UnnamedPackage
 */

public class UnnamedPackage {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Package pkg = UnnamedPackage.class.getPackage();
        check(pkg == null, "unnamed package has no Package object");
        check("UnnamedPackage".equals(UnnamedPackage.class.getName()),
              "fully qualified name of an unnamed-package type is its simple name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
