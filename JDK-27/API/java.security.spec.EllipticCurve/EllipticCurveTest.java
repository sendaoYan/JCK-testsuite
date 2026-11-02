/*
 * @test
 * @summary java.security.spec.EllipticCurve conformance: constructors and accessors.
 * @run main EllipticCurveTest
 */

import java.security.spec.EllipticCurve;
import java.security.spec.ECFieldFp;
import java.math.BigInteger;

public class EllipticCurveTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        EllipticCurve c = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        check(field.equals(c.getField()), "getField");
        check(BigInteger.ONE.equals(c.getA()), "getA");
        check(BigInteger.ONE.equals(c.getB()), "getB");
        check(c.getSeed() == null, "getSeed null");
        EllipticCurve c2 = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        check(c.equals(c2), "equals");
        check(c.hashCode() == c2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
