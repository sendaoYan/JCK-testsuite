/*
 * @test
 * @summary javax.print.attribute.standard.QueuedJobCount conformance: integer attribute.
 * @run main QueuedJobCountTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class QueuedJobCountTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        QueuedJobCount v = new QueuedJobCount(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == QueuedJobCount.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new QueuedJobCount(10)), "equals");
        check(!v.equals(new QueuedJobCount(20)), "not equals");
        check(v.hashCode() == new QueuedJobCount(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
