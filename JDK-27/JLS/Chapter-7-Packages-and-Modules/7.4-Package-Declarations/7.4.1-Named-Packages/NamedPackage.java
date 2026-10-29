/*
 * @test
 * @summary JLS 7.4.1: a package declaration specifies the fully qualified name
 *          of the package to which the compilation unit belongs.
 * @run main p.NamedPackage
 */
package p;

public class NamedPackage {
    public static void main(String[] args) {
        if (!"p".equals(NamedPackage.class.getPackageName()))
            throw new AssertionError("package name");

        System.out.println("PASS");
    }
}
