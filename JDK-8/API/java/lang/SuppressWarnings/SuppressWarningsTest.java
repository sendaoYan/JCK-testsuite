/*
 * @test
 * @summary java.lang.SuppressWarnings API conformance: SOURCE retention, TYPE target, and a
 *          value() member of type String[].
 * @run main SuppressWarningsTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SuppressWarningsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRetention() {
        check(SuppressWarnings.class.getAnnotation(Retention.class).value()
              == RetentionPolicy.SOURCE, "SOURCE retention");
    }

    static void testValueType() throws Exception {
        check(SuppressWarnings.class.getMethod("value").getReturnType() == String[].class,
              "value() returns String[]");
    }

    public static void main(String[] args) throws Exception {
        testRetention();
        testValueType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
