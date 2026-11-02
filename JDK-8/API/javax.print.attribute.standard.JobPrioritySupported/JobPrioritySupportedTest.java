/*
 * @test
 * @summary javax.print.attribute.standard.JobPrioritySupported conformance: integer attribute.
 * @run main JobPrioritySupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobPrioritySupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobPrioritySupported v = new JobPrioritySupported(5);
        check(v.getValue() == 5, "getValue");
        check(v.getCategory() == JobPrioritySupported.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobPrioritySupported(5)), "equals");
        check(!v.equals(new JobPrioritySupported(7)), "not equals");
        check(v.hashCode() == new JobPrioritySupported(5).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
