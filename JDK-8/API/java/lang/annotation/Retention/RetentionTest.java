/*
 * @test
 * @summary java.lang.annotation.Retention conformance: RUNTIME-retained meta-annotation.
 * @run main RetentionTest
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RetentionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRetention() {
        check(Retention.class.isAnnotation(), "Retention is annotation");
        Retention r = Retention.class.getAnnotation(Retention.class);
        check(r != null, "Retention is self-annotated");
        check(r.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
    }

    public static void main(String[] args) {
        testRetention();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
