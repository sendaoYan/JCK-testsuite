/*
 * @test
 * @summary java.security.spec.DSAPrivateKeySpec conformance: constructor and accessors.
 * @run main DSAPrivateKeySpecTest
 */

import java.security.spec.DSAPrivateKeySpec;
import java.math.BigInteger;

public class DSAPrivateKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        DSAPrivateKeySpec spec = new DSAPrivateKeySpec(new BigInteger("1"), new BigInteger("2"), new BigInteger("3"), new BigInteger("4"));
        check(new BigInteger("1").equals(spec.getX()), "getX");
        check(new BigInteger("2").equals(spec.getP()), "getP");
        check(new BigInteger("3").equals(spec.getQ()), "getQ");
        check(new BigInteger("4").equals(spec.getG()), "getG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
