/*
 * @test
 * @summary javax.print.attribute.standard.JobMediaSheetsCompleted conformance: integer attribute.
 * @run main JobMediaSheetsCompletedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobMediaSheetsCompletedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobMediaSheetsCompleted v = new JobMediaSheetsCompleted(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == JobMediaSheetsCompleted.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobMediaSheetsCompleted(10)), "equals");
        check(!v.equals(new JobMediaSheetsCompleted(20)), "not equals");
        check(v.hashCode() == new JobMediaSheetsCompleted(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
