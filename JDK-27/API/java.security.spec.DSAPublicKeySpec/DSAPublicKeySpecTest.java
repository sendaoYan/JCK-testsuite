/*
 * @test
 * @summary java.security.spec.DSAPublicKeySpec conformance: constructor and accessors.
 * @run main DSAPublicKeySpecTest
 */

import java.security.spec.DSAPublicKeySpec;
import java.math.BigInteger;

public class DSAPublicKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        DSAPublicKeySpec spec = new DSAPublicKeySpec(new BigInteger("1"), new BigInteger("2"), new BigInteger("3"), new BigInteger("4"));
        check(new BigInteger("1").equals(spec.getY()), "getY");
        check(new BigInteger("2").equals(spec.getP()), "getP");
        check(new BigInteger("3").equals(spec.getQ()), "getQ");
        check(new BigInteger("4").equals(spec.getG()), "getG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
