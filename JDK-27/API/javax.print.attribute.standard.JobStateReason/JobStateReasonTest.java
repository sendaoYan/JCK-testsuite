/*
 * @test
 * @summary javax.print.attribute.standard.JobStateReason conformance: enum attribute.
 * @run main JobStateReasonTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobStateReasonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobStateReason v1 = JobStateReason.JOB_INCOMING;
        check(v1 instanceof JobStateReason, "JOB_INCOMING instanceof JobStateReason");
        check(v1.getCategory() == JobStateReason.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        JobStateReason v2 = JobStateReason.JOB_PRINTING;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(JobStateReason.JOB_INCOMING), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == JobStateReason.JOB_INCOMING.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
