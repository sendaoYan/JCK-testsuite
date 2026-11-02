/*
 * @test
 * @summary javax.crypto.spec.RC5ParameterSpec conformance: RC5 parameter spec accessors.
 * @run main RC5ParameterSpecTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class RC5ParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] iv = new byte[8];
        RC5ParameterSpec s = new RC5ParameterSpec(0, 16, 32, iv);
        check(s.getVersion() == 0, "getVersion");
        check(s.getRounds() == 16, "getRounds");
        check(s.getWordSize() == 32, "getWordSize");
        check(Arrays.equals(iv, s.getIV()), "getIV");
        check(s.equals(new RC5ParameterSpec(0, 16, 32, iv)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
