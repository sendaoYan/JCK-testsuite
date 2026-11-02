/*
 * @test
 * @summary java.security.spec.DSAParameterSpec conformance: constructor and accessors.
 * @run main DSAParameterSpecTest
 */

import java.security.spec.DSAParameterSpec;
import java.math.BigInteger;

public class DSAParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BigInteger p = new BigInteger("23"), q = new BigInteger("11"), g = new BigInteger("5");
        DSAParameterSpec spec = new DSAParameterSpec(p, q, g);
        check(p.equals(spec.getP()), "getP");
        check(q.equals(spec.getQ()), "getQ");
        check(g.equals(spec.getG()), "getG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
