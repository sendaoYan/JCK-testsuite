/*
 * @test
 * @summary java.security.spec.ECFieldF2m conformance: constructors and accessors.
 * @run main ECFieldF2mTest
 */

import java.security.spec.ECFieldF2m;
import java.math.BigInteger;

public class ECFieldF2mTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ECFieldF2m f = new ECFieldF2m(5);
        check(f.getFieldSize() == 5, "getFieldSize");
        check(f.getM() == 5, "getM");
        check(f.getMidTermsOfReductionPolynomial() == null, "no mid terms");
        check(f.getReductionPolynomial() == null, "no reduction polynomial");

        ECFieldF2m f2 = new ECFieldF2m(5, new int[] { 1 });
        check(f2.getM() == 5, "getM with mid terms");
        check(f2.getMidTermsOfReductionPolynomial().length == 1, "mid terms length");
        check(f2.equals(new ECFieldF2m(5, new int[] { 1 })), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
