/*
 * @test
 * @summary java.security.spec.EdECPublicKeySpec conformance: constructor and accessors.
 * @run main EdECPublicKeySpecTest
 */

import java.security.spec.EdECPublicKeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.EdECPoint;
import java.math.BigInteger;

public class EdECPublicKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        EdECPoint point = new EdECPoint(false, BigInteger.TEN);
        EdECPublicKeySpec spec = new EdECPublicKeySpec(NamedParameterSpec.ED25519, point);
        check(NamedParameterSpec.ED25519.equals(spec.getParams()), "getParams");
        check(point.equals(spec.getPoint()), "getPoint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
