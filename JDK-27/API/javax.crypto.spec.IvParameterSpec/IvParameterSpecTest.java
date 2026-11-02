/*
 * @test
 * @summary javax.crypto.spec.IvParameterSpec conformance: IV accessor.
 * @run main IvParameterSpecTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class IvParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] iv = new byte[16];
        IvParameterSpec s = new IvParameterSpec(iv);
        check(Arrays.equals(iv, s.getIV()), "getIV");
        IvParameterSpec s2 = new IvParameterSpec(iv, 4, 8);
        check(s2.getIV().length == 8, "offset ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
