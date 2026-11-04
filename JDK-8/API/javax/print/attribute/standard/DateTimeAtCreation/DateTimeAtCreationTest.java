/*
 * @test
 * @summary javax.print.attribute.standard.DateTimeAtCreation conformance: date-time attribute.
 * @run main DateTimeAtCreationTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class DateTimeAtCreationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        DateTimeAtCreation v = new DateTimeAtCreation(new java.util.Date(0L));
        check(new java.util.Date(0L).equals(v.getValue()), "getValue");
        check(v.getCategory() == DateTimeAtCreation.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new DateTimeAtCreation(new java.util.Date(0L))), "equals");
        check(v.hashCode() == new DateTimeAtCreation(new java.util.Date(0L)).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
