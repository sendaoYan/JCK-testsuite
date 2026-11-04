/*
 * @test
 * @summary javax.print.attribute.standard.JobStateReasons conformance: job state reasons set.
 * @run main JobStateReasonsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobStateReasonsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobStateReasons v = new JobStateReasons();
        check(v.isEmpty(), "initially empty");
        check(v.add(JobStateReason.JOB_PRINTING), "add");
        check(v.contains(JobStateReason.JOB_PRINTING), "contains");
        check(v.size() == 1, "size");
        check(v.getCategory() == JobStateReasons.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.remove(JobStateReason.JOB_PRINTING), "remove");
        check(v.isEmpty(), "empty after remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
