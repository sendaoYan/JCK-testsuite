/*
 * @test
 * @summary JLS 6.5.3.1 (Java SE 27): a simple package name denotes the package
 *          declared by a top-level package declaration with that name; it may
 *          be used as the qualifier of a qualified type name.
 * @compile C.java SimplePackageName.java
 * @run main SimplePackageName
 */

public class SimplePackageName {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        spnpkg.C c = new spnpkg.C();   // spnpkg is a simple package name
        check(spnpkg.C.k == 5, "simple package name as a qualifier");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
