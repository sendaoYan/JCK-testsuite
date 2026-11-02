/*
 * @test
 * @summary javax.print.attribute.standard.NumberOfInterveningJobs conformance: integer attribute.
 * @run main NumberOfInterveningJobsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class NumberOfInterveningJobsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        NumberOfInterveningJobs v = new NumberOfInterveningJobs(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == NumberOfInterveningJobs.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new NumberOfInterveningJobs(10)), "equals");
        check(!v.equals(new NumberOfInterveningJobs(20)), "not equals");
        check(v.hashCode() == new NumberOfInterveningJobs(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
