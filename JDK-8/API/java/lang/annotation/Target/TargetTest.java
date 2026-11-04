/*
 * @test
 * @summary java.lang.annotation.Target conformance: RUNTIME-retained, ANNOTATION_TYPE-targeted.
 * @run main TargetTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class TargetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTarget() {
        check(Target.class.isAnnotation(), "Target is annotation");
        Retention r = Target.class.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME, "Target has @Retention(RUNTIME)");
        Target t = Target.class.getAnnotation(Target.class);
        check(t != null, "Target is self-annotated");
        check(t.value().length == 1 && t.value()[0] == ElementType.ANNOTATION_TYPE,
              "@Target(ANNOTATION_TYPE)");
    }

    public static void main(String[] args) {
        testTarget();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
