/*
 * @test
 * @summary java.lang.ClassValue API conformance: per-class computed and cached values.
 * @run main ClassValueTest
 */

public class ClassValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClassValue() {
        ClassValue<String> cv = new ClassValue<String>() {
            protected String computeValue(Class<?> type) {
                return type.getName();
            }
        };
        check("java.lang.String".equals(cv.get(String.class)), "get computes the value");
        check("java.lang.String".equals(cv.get(String.class)), "get caches the value");
        check("java.lang.Integer".equals(cv.get(Integer.class)), "get is per-class");
    }

    public static void main(String[] args) {
        testClassValue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
