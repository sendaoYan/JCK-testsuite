/*
 * @test
 * @summary JLS 7.5.4: a static-import-on-demand declaration imports all
 *          accessible static members of a named class or interface as needed.
 * @run main StaticImportOnDemand
 */

import static java.lang.Math.*;

public class StaticImportOnDemand {
    public static void main(String[] args) {
        double d = sqrt(16.0);
        double m = max(3.0, 4.0);
        if (d != 4.0) throw new AssertionError("sqrt");
        if (m != 4.0) throw new AssertionError("max");
        if (PI != java.lang.Math.PI) throw new AssertionError("PI");

        System.out.println("PASS");
    }
}
