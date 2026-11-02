/*
 * @test
 * @summary javax.crypto.spec.PBEParameterSpec conformance: PBE parameter spec salt and iteration.
 * @run main PBEParameterSpecTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class PBEParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] salt = new byte[]{1, 2, 3};
        PBEParameterSpec s = new PBEParameterSpec(salt, 1000);
        check(Arrays.equals(salt, s.getSalt()), "getSalt");
        check(s.getIterationCount() == 1000, "getIterationCount");
        check(s.getParameterSpec() == null, "default parameter spec null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
