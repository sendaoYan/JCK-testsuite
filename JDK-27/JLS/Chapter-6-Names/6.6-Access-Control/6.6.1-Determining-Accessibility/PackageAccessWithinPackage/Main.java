/*
 * @test
 * @summary JLS 6.6.1: a package-private class and package-private member are
 *          accessible from within the same package.
 * @compile Pkg.java Main.java
 * @run main same.Main
 */
package same;

public class Main {
    public static void main(String[] args) {
        Pkg p = new Pkg();
        if (p.pack != 5) throw new AssertionError("package access within package");

        System.out.println("PASS");
    }
}
