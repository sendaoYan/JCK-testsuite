/*
 * @test
 * @summary java.beans.Transient conformance: annotation shape.
 * @run main TransientTest
 */

import java.beans.Transient;

public class TransientTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(Transient.class.isAnnotation(), "annotation");
        check(Transient.class.getAnnotation(java.lang.annotation.Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME, "runtime retention");
        check(Transient.class.getMethod("value").getReturnType() == boolean.class, "boolean value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
