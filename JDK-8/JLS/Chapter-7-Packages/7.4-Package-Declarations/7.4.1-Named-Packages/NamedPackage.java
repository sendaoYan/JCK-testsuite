/*
 * @test
 * @summary JLS 7.4.1 (Java SE 8): a package declaration specifies the fully
 *          qualified name of the named package; a top level type's fully
 *          qualified name is the package name followed by its simple name.
 * @run main vista.NamedPackage
 */
package vista;

public class NamedPackage {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Package pkg = NamedPackage.class.getPackage();
        check(pkg != null, "named package has a Package object");
        check("vista".equals(pkg.getName()), "package name: " + pkg.getName());
        check("vista.NamedPackage".equals(NamedPackage.class.getName()),
              "fully qualified name is package + simple name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
