/*
 * @test
 * @summary jdk.jfr.Frequency conformance: annotation with runtime retention.
 * @run main FrequencyTest
 */

import jdk.jfr.Frequency;

public class FrequencyTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(Frequency.class.isAnnotation(), "annotation");
        check(Frequency.class.getAnnotation(java.lang.annotation.Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME, "runtime retention");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
