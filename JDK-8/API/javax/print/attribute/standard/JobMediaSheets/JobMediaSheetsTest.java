/*
 * @test
 * @summary javax.print.attribute.standard.JobMediaSheets conformance: integer attribute.
 * @run main JobMediaSheetsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobMediaSheetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobMediaSheets v = new JobMediaSheets(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == JobMediaSheets.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobMediaSheets(10)), "equals");
        check(!v.equals(new JobMediaSheets(20)), "not equals");
        check(v.hashCode() == new JobMediaSheets(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
