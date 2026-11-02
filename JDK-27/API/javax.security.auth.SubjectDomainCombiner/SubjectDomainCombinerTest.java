/*
 * @test
 * @summary javax.security.auth.SubjectDomainCombiner conformance: subject domain combiner.
 * @run main SubjectDomainCombinerTest
 */

import javax.security.auth.*;
import java.security.*;

public class SubjectDomainCombinerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Subject sub = new Subject();
        SubjectDomainCombiner sdc = new SubjectDomainCombiner(sub);
        check(sdc.getSubject() == sub, "getSubject");
        ProtectionDomain[] r = sdc.combine(new ProtectionDomain[0], new ProtectionDomain[0]);
        check(r != null && r.length == 0, "combine empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
