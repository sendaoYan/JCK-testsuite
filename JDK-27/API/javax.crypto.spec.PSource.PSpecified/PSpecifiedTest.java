/*
 * @test
 * @summary javax.crypto.spec.PSource.PSpecified conformance: PSpecified value and DEFAULT.
 * @run main PSpecifiedTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class PSpecifiedTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(PSource.PSpecified.DEFAULT != null, "DEFAULT non-null");
        PSource.PSpecified s = new PSource.PSpecified(new byte[]{1, 2});
        check("PSpecified".equals(s.getAlgorithm()), "getAlgorithm");
        check(Arrays.equals(new byte[]{1, 2}, s.getValue()), "getValue");
        check(PSource.PSpecified.DEFAULT.getValue().length == 0, "DEFAULT empty value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
