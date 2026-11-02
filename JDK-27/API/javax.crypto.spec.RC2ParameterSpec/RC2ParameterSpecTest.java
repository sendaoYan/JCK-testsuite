/*
 * @test
 * @summary javax.crypto.spec.RC2ParameterSpec conformance: RC2 parameter spec bits and IV.
 * @run main RC2ParameterSpecTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class RC2ParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] iv = new byte[8];
        RC2ParameterSpec s = new RC2ParameterSpec(64, iv);
        check(s.getEffectiveKeyBits() == 64, "getEffectiveKeyBits");
        check(Arrays.equals(iv, s.getIV()), "getIV");
        check(new RC2ParameterSpec(64).getIV() == null, "no IV is null");
        check(s.equals(new RC2ParameterSpec(64, iv)), "equals");
        check(s.hashCode() == new RC2ParameterSpec(64, iv).hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
