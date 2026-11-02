/*
 * @test
 * @summary jdk.jfr.DataAmount conformance: annotation with runtime retention.
 * @run main DataAmountTest
 */

import jdk.jfr.DataAmount;

public class DataAmountTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(DataAmount.class.isAnnotation(), "annotation");
        check(DataAmount.class.getAnnotation(java.lang.annotation.Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME, "runtime retention");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
