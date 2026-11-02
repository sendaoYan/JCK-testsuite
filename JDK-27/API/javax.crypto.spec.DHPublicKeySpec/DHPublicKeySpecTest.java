/*
 * @test
 * @summary javax.crypto.spec.DHPublicKeySpec conformance: DH public key spec accessors.
 * @run main DHPublicKeySpecTest
 */

import javax.crypto.spec.*;
import java.math.BigInteger;

public class DHPublicKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BigInteger y = BigInteger.valueOf(4);
        BigInteger p = BigInteger.valueOf(23);
        BigInteger g = BigInteger.valueOf(5);
        DHPublicKeySpec s = new DHPublicKeySpec(y, p, g);
        check(s.getY().equals(y) && s.getP().equals(p) && s.getG().equals(g), "accessors");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
