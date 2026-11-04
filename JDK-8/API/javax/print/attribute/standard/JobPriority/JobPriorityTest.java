/*
 * @test
 * @summary javax.print.attribute.standard.JobPriority conformance: integer attribute.
 * @run main JobPriorityTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobPriorityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobPriority v = new JobPriority(5);
        check(v.getValue() == 5, "getValue");
        check(v.getCategory() == JobPriority.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobPriority(5)), "equals");
        check(!v.equals(new JobPriority(7)), "not equals");
        check(v.hashCode() == new JobPriority(5).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
