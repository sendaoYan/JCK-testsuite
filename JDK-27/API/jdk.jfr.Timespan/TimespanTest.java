/*
 * @test
 * @summary jdk.jfr.Timespan conformance: annotation with runtime retention.
 * @run main TimespanTest
 */

import jdk.jfr.Timespan;

public class TimespanTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(Timespan.class.isAnnotation(), "annotation");
        check(Timespan.class.getAnnotation(java.lang.annotation.Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME, "runtime retention");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
