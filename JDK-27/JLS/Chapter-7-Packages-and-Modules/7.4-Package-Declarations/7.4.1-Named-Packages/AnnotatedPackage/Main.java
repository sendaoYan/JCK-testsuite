/*
 * @test
 * @summary JLS 7.4.1: at most one annotated package declaration is permitted for
 *          a given package; it is conventionally placed in package-info.java.
 * @compile PkgAnno.java package-info.java Main.java
 * @run main p.Main
 */
package p;

public class Main {
    public static void main(String[] args) {
        Package pkg = Main.class.getPackage();
        PkgAnno a = pkg.getAnnotation(PkgAnno.class);
        if (a == null) throw new AssertionError("package annotation missing");
        if (!"hello".equals(a.value())) throw new AssertionError("annotation value");

        System.out.println("PASS");
    }
}
