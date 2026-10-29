/*
 * @test
 * @summary JLS 7.4.2: an ordinary compilation unit with no package declaration
 *          is part of an unnamed package.
 * @run main UnnamedPackage
 */

public class UnnamedPackage {
    public static void main(String[] args) {
        if (!"".equals(UnnamedPackage.class.getPackageName()))
            throw new AssertionError("unnamed package name");

        System.out.println("PASS");
    }
}
