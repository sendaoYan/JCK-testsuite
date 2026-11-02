/*
 * @test
 * @summary java.security.spec.ECPublicKeySpec conformance: constructor and accessors.
 * @run main ECPublicKeySpecTest
 */

import java.security.spec.ECPublicKeySpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.security.spec.ECFieldFp;
import java.security.spec.ECPoint;
import java.math.BigInteger;

public class ECPublicKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        EllipticCurve curve = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        ECParameterSpec params = new ECParameterSpec(curve, new ECPoint(BigInteger.ONE, BigInteger.ONE), new BigInteger("7"), 1);
        ECPoint w = new ECPoint(BigInteger.ONE, BigInteger.TWO);
        ECPublicKeySpec spec = new ECPublicKeySpec(w, params);
        check(w.equals(spec.getW()), "getW");
        check(params.equals(spec.getParams()), "getParams");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
