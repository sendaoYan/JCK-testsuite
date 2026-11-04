/*
 * @test
 * @summary JLS 7.6 (Java SE 8): in the absence of an access modifier, a top
 *          level type has package access and is accessible within compilation
 *          units of the same package.
 * @compile p/PackagePrivate.java PackagePrivateTopLevelType.java
 * @run main p.PackagePrivateTopLevelType
 */
package p;

public class PackagePrivateTopLevelType {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        PackagePrivate pp = new PackagePrivate();
        check(pp.id == 3, "package-private type accessible within same package");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
