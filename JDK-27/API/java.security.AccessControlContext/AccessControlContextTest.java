/*
 * @test
 * @summary java.security.AccessControlContext conformance: constructors and getDomainCombiner.
 * @run main AccessControlContextTest
 */

import java.security.AccessControlContext;
import java.security.ProtectionDomain;

public class AccessControlContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[0]);
        check(acc.getDomainCombiner() == null, "getDomainCombiner null");
        AccessControlContext acc2 = new AccessControlContext(acc, null);
        check(acc2.getDomainCombiner() == null, "2-arg getDomainCombiner null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
