/*
 * @test
 * @summary JLS 6.5.3.2 (Java SE 27): a qualified name Q.Id is a package name if Q
 *          is a package name and Id is a subpackage; a qualified package name
 *          may qualify a type name.
 * @compile C.java QualifiedPackageName.java
 * @run main QualifiedPackageName
 */

public class QualifiedPackageName {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        qpnpkg.sub.C c = new qpnpkg.sub.C();   // qpnpkg.sub is a qualified package name
        check(qpnpkg.sub.C.k == 7, "qualified package name as a qualifier");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
