/*
 * @test
 * @summary javax.print.attribute.standard.DateTimeAtProcessing conformance: date-time attribute.
 * @run main DateTimeAtProcessingTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class DateTimeAtProcessingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        DateTimeAtProcessing v = new DateTimeAtProcessing(new java.util.Date(0L));
        check(new java.util.Date(0L).equals(v.getValue()), "getValue");
        check(v.getCategory() == DateTimeAtProcessing.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new DateTimeAtProcessing(new java.util.Date(0L))), "equals");
        check(v.hashCode() == new DateTimeAtProcessing(new java.util.Date(0L)).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
