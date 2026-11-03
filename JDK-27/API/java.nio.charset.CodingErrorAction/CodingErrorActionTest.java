/*
 * @test
 * @summary java.nio.charset.CodingErrorAction conformance: white-box structure + black-box constants.
 * @run main CodingErrorActionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.CodingErrorAction;

public class CodingErrorActionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.CodingErrorAction");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "superclass is Object");

        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly one declared constructor");
        check(Modifier.isPrivate(ctors[0].getModifiers()), "constructor is private");

        int constants = 0;
        for (Field f : c.getFields()) {
            if (f.getType() == CodingErrorAction.class && Modifier.isStatic(f.getModifiers())) {
                constants++;
            }
        }
        check(constants == 3, "exactly three public constants");
    }

    static void testBlackBox() {
        check("IGNORE".equals(CodingErrorAction.IGNORE.toString()), "IGNORE");
        check("REPLACE".equals(CodingErrorAction.REPLACE.toString()), "REPLACE");
        check("REPORT".equals(CodingErrorAction.REPORT.toString()), "REPORT");
        check(CodingErrorAction.IGNORE != CodingErrorAction.REPLACE, "IGNORE != REPLACE");
        check(CodingErrorAction.REPLACE != CodingErrorAction.REPORT, "REPLACE != REPORT");
        check(CodingErrorAction.IGNORE != CodingErrorAction.REPORT, "IGNORE != REPORT");
        check(CodingErrorAction.IGNORE.equals(CodingErrorAction.IGNORE), "constant identity/equality");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
