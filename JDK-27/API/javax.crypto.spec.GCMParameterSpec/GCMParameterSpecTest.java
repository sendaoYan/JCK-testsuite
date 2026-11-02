/*
 * @test
 * @summary javax.crypto.spec.GCMParameterSpec conformance: GCM tag length and IV accessors.
 * @run main GCMParameterSpecTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class GCMParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] iv = new byte[12];
        GCMParameterSpec s = new GCMParameterSpec(128, iv);
        check(s.getTLen() == 128, "getTLen");
        check(Arrays.equals(iv, s.getIV()), "getIV");
        GCMParameterSpec s2 = new GCMParameterSpec(96, iv, 4, 8);
        check(s2.getTLen() == 96 && s2.getIV().length == 8, "offset ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
