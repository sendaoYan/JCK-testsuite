/*
 * @test
 * @summary java.security.spec.ECFieldFp conformance: constructor, getP, getFieldSize.
 * @run main ECFieldFpTest
 */

import java.security.spec.ECFieldFp;
import java.math.BigInteger;

public class ECFieldFpTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BigInteger p = new BigInteger("23");
        ECFieldFp f = new ECFieldFp(p);
        check(p.equals(f.getP()), "getP");
        check(f.getFieldSize() == 5, "getFieldSize 5 (23 < 32)");
        ECFieldFp f2 = new ECFieldFp(new BigInteger("23"));
        check(f.equals(f2), "equals");
        check(f.hashCode() == f2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
