/*
 * @test
 * @summary JLS 7.4.1 (Java SE 8): a package declaration may be annotated; the
 *          annotations are placed in a package-info.java compilation unit and
 *          are retrievable reflectively.
 * @compile PkgAnno.java package-info.java Main.java
 * @run main annotated.Main
 */
package annotated;

public class Main {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Package pkg = Main.class.getPackage();
        PkgAnno a = pkg.getAnnotation(PkgAnno.class);
        check(a != null, "package annotation present");
        check("package-level".equals(a.value()), "package annotation value");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
