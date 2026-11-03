/*
 * @test
 * @summary java.lang.ClassValue conformance: white-box structure + black-box behavior.
 * @run main ClassValueTest
 */

import java.lang.reflect.Modifier;

public class ClassValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ClassValue");
        check(Modifier.isAbstract(c.getModifiers()), "ClassValue is abstract");
        check(!c.isInterface(), "ClassValue is a class");
        check(Modifier.isProtected(c.getDeclaredMethod("computeValue", Class.class).getModifiers()),
              "computeValue is protected");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testClassValue() {
        final int[] computed = {0};
        ClassValue<String> cv = new ClassValue<String>() {
            protected String computeValue(Class<?> type) {
                computed[0]++;
                return type.getName();
            }
        };
        check("java.lang.String".equals(cv.get(String.class)), "get computes the value");
        check("java.lang.String".equals(cv.get(String.class)), "get caches the value");
        check(computed[0] == 1, "computeValue called exactly once per class");
        check("java.lang.Integer".equals(cv.get(Integer.class)), "get is per-class");
        check(computed[0] == 2, "computeValue called for a second class");
    }

    static void testRemove() {
        ClassValue<String> cv = new ClassValue<String>() {
            protected String computeValue(Class<?> type) { return type.getSimpleName(); }
        };
        check("String".equals(cv.get(String.class)), "get before remove");
        cv.remove(String.class);
        check("String".equals(cv.get(String.class)), "get recomputes after remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testClassValue();
        testRemove();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
