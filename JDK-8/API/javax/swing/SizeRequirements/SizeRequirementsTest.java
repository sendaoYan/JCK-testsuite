/*
 * @test
 * @summary javax.swing.SizeRequirements conformance: size fields (headless-safe).
 * @run main SizeRequirementsTest
 */

import javax.swing.SizeRequirements;

public class SizeRequirementsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        SizeRequirements r = new SizeRequirements();
        r.minimum = 10; r.preferred = 20; r.maximum = 30;
        check(r.minimum == 10 && r.preferred == 20 && r.maximum == 30, "fields");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
