/*
 * @test
 * @summary java.util.ServiceConfigurationError conformance: white-box structure + black-box behavior.
 * @run main ServiceConfigurationErrorTest
 */

import java.util.ServiceConfigurationError;

public class ServiceConfigurationErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.ServiceConfigurationError");
        check(c.getSuperclass() == Error.class, "extends Error");
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
    }

    static void testBlackBox() {
        check("msg".equals(new ServiceConfigurationError("msg").getMessage()), "String ctor message");
        Throwable cause = new IllegalStateException("c");
        check(cause == new ServiceConfigurationError("m", cause).getCause(), "cause preserved");
        boolean caught = false;
        try {
            throw new ServiceConfigurationError("e");
        } catch (Error e) {
            caught = true;
        }
        check(caught, "catchable as Error");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
