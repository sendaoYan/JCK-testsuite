/*
 * @test
 * @summary javax.security.auth.SubjectDomainCombiner conformance: constructor, getSubject, combine.
 * @run main SubjectDomainCombinerTest
 */

public class SubjectDomainCombinerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.Subject subject = new javax.security.auth.Subject();
        javax.security.auth.SubjectDomainCombiner c = new javax.security.auth.SubjectDomainCombiner(subject);
        check(subject.equals(c.getSubject()), "getSubject");
        java.security.ProtectionDomain[] out = c.combine(new java.security.ProtectionDomain[0], new java.security.ProtectionDomain[0]);
        check(out != null, "combine non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
