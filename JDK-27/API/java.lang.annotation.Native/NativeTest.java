/*
 * @test
 * @summary java.lang.annotation.Native conformance: SOURCE-retained, FIELD-targeted annotation.
 * @run main NativeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Native;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class NativeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNative() {
        check(Native.class.isAnnotation(), "Native is annotation");
        Retention r = Native.class.getAnnotation(Retention.class);
        check(r != null, "Native has @Retention");
        check(r.value() == RetentionPolicy.SOURCE, "@Retention(SOURCE)");
        Target t = Native.class.getAnnotation(Target.class);
        check(t != null, "Native has @Target");
        check(t.value().length == 1 && t.value()[0] == ElementType.FIELD, "@Target(FIELD)");
    }

    public static void main(String[] args) {
        testNative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
