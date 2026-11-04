/*
 * @test
 * @summary java.security.AccessControlContext conformance: constructors, getDomainCombiner, equals/hashCode.
 * @run main AccessControlContextTest
 */

public class AccessControlContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.ProtectionDomain[] pd = new java.security.ProtectionDomain[0];
        java.security.AccessControlContext acc = new java.security.AccessControlContext(pd);
        check(acc.getDomainCombiner() == null, "getDomainCombiner null");
        check(acc.equals(acc), "equals self");
        check(acc.hashCode() == acc.hashCode(), "hashCode");
        java.security.AccessControlContext acc2 = new java.security.AccessControlContext(acc, null);
        check(acc2.getDomainCombiner() == null, "2-arg ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
