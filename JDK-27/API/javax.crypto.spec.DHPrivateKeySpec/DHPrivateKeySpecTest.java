/*
 * @test
 * @summary javax.crypto.spec.DHPrivateKeySpec conformance: DH private key spec accessors.
 * @run main DHPrivateKeySpecTest
 */

import javax.crypto.spec.*;
import java.math.BigInteger;

public class DHPrivateKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BigInteger x = BigInteger.valueOf(4);
        BigInteger p = BigInteger.valueOf(23);
        BigInteger g = BigInteger.valueOf(5);
        DHPrivateKeySpec s = new DHPrivateKeySpec(x, p, g);
        check(s.getX().equals(x) && s.getP().equals(p) && s.getG().equals(g), "accessors");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
