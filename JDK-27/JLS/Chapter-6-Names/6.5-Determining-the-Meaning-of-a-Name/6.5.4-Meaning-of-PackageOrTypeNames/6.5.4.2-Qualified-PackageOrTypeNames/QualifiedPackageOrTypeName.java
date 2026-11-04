/*
 * @test
 * @summary JLS 6.5.4.2 (Java SE 27): a qualified PackageOrTypeName Q.Id denotes a
 *          member type when Q is a type, and a top-level type or subpackage
 *          when Q is a package.
 * @run main QualifiedPackageOrTypeName
 */

public class QualifiedPackageOrTypeName {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static class Holder {
        static class Inner { static int k = 3; }
    }

    public static void main(String[] args) {
        // Q is a package: java.util.Date is a top-level type of package java.util
        java.util.Date d = new java.util.Date(0);
        check(d.getTime() == 0, "package-qualified top-level type");

        // Q is a type: Holder.Inner is a member type of type Holder
        check(Holder.Inner.k == 3, "type-qualified member type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
