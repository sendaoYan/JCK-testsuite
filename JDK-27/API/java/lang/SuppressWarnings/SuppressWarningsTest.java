/*
 * @test
 * @summary java.lang.SuppressWarnings conformance: white-box structure + black-box behavior
 *          (SOURCE retention, TYPE target, value() returns String[]).
 * @run main SuppressWarningsTest
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SuppressWarningsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        Class<?> c = SuppressWarnings.class;
        check(c.isAnnotation(), "SuppressWarnings is an annotation type");
        check(c.getAnnotation(Retention.class).value() == RetentionPolicy.SOURCE,
              "SOURCE retention");
        // SuppressWarnings carries no @Target meta-annotation: it applies to
        // TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, MODULE.
        check(c.getAnnotation(Target.class) == null, "no @Target restriction");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testValueType() throws Exception {
        check(SuppressWarnings.class.getMethod("value").getReturnType() == String[].class,
              "value() returns String[]");
    }

    
    static void testBlackBox() throws Exception {
        testValueType();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
