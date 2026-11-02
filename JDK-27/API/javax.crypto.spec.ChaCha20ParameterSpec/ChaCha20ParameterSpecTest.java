/*
 * @test
 * @summary javax.crypto.spec.ChaCha20ParameterSpec conformance: ChaCha20 nonce and counter accessors.
 * @run main ChaCha20ParameterSpecTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class ChaCha20ParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] nonce = new byte[12];
        ChaCha20ParameterSpec s = new ChaCha20ParameterSpec(nonce, 7);
        check(Arrays.equals(nonce, s.getNonce()), "getNonce");
        check(s.getCounter() == 7, "getCounter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
