/*
 * @test
 * @summary java.beans.ConstructorProperties conformance: white-box structure + black-box behavior.
 * @run main ConstructorPropertiesTest
 */

import java.beans.ConstructorProperties;

public class ConstructorPropertiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Bean {
        private final int a; private final String b;
        @ConstructorProperties({"a", "b"})
        public Bean(int a, String b) { this.a = a; this.b = b; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.ConstructorProperties");
        check(c.isAnnotation(), "is an annotation");
        check(c.getMethod("value").getReturnType() == String[].class, "value returns String[]");
    }

    static void testBlackBox() throws Exception {
        ConstructorProperties cp = Bean.class.getConstructor(int.class, String.class)
            .getAnnotation(ConstructorProperties.class);
        check(cp != null, "annotation present");
        check(java.util.Arrays.equals(new String[]{"a", "b"}, cp.value()), "value array");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
