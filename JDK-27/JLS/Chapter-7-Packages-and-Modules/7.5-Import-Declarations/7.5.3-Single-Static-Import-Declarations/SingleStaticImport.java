/*
 * @test
 * @summary JLS 7.5.3: a single-static-import declaration imports accessible
 *          static members with a given simple name from a class or interface.
 * @run main SingleStaticImport
 */

import static java.lang.Math.PI;

public class SingleStaticImport {
    public static void main(String[] args) {
        if (PI != Math.PI) throw new AssertionError("single static import of PI");

        System.out.println("PASS");
    }
}
