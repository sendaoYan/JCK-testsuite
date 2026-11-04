/*
 * @test
 * @summary java.beans.Transient conformance: white-box structure + black-box behavior.
 * @run main TransientTest
 */

import java.beans.Transient;

public class TransientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Bean {
        @Transient public int getA() { return 1; }
        @Transient(false) public int getB() { return 2; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Transient");
        check(c.isAnnotation(), "is an annotation");
        check(c.getMethod("value").getReturnType() == boolean.class, "value returns boolean");
    }

    static void testBlackBox() throws Exception {
        java.lang.reflect.Method ma = Bean.class.getMethod("getA");
        java.lang.reflect.Method mb = Bean.class.getMethod("getB");
        check(ma.getAnnotation(Transient.class).value(), "value default true");
        check(!mb.getAnnotation(Transient.class).value(), "value false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
