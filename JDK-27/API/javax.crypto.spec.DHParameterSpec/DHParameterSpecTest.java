/*
 * @test
 * @summary javax.crypto.spec.DHParameterSpec conformance: DH parameter accessors.
 * @run main DHParameterSpecTest
 */

import javax.crypto.spec.*;
import java.math.BigInteger;

public class DHParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BigInteger p = BigInteger.valueOf(23);
        BigInteger g = BigInteger.valueOf(5);
        DHParameterSpec s = new DHParameterSpec(p, g, 8);
        check(s.getP().equals(p) && s.getG().equals(g), "getP/getG");
        check(s.getL() == 8, "getL");
        check(new DHParameterSpec(p, g).getL() == 0, "default L 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
