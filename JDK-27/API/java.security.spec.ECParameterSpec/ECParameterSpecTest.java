/*
 * @test
 * @summary java.security.spec.ECParameterSpec conformance: constructor and accessors.
 * @run main ECParameterSpecTest
 */

import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.security.spec.ECFieldFp;
import java.security.spec.ECPoint;
import java.math.BigInteger;

public class ECParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        EllipticCurve curve = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        ECPoint g = new ECPoint(BigInteger.ONE, BigInteger.ONE);
        ECParameterSpec spec = new ECParameterSpec(curve, g, new BigInteger("7"), 1);
        check(curve.equals(spec.getCurve()), "getCurve");
        check(g.equals(spec.getGenerator()), "getGenerator");
        check(new BigInteger("7").equals(spec.getOrder()), "getOrder");
        check(spec.getCofactor() == 1, "getCofactor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
