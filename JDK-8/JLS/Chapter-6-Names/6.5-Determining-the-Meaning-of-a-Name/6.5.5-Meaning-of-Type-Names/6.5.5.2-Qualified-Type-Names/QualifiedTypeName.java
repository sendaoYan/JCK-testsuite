/*
 * @test
 * @summary JLS 6.5.5.2 (Java SE 8): a qualified type name Q.Id denotes a member
 *          type of the package or type Q when Q is a package name or type name.
 * @run main QualifiedTypeName
 */

public class QualifiedTypeName {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static class Outer {
        static class Inner { static int k = 13; }
    }

    public static void main(String[] args) {
        java.util.Date d = new java.util.Date(0);   // package-qualified type
        check(d.getTime() == 0, "package-qualified type name");

        check(Outer.Inner.k == 13, "type-qualified member type name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
