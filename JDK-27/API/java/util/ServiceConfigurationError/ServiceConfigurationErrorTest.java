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


    // ===================== 契约深化：Error 层次/cause 保持/字符串形式 =====================
    static void testSceContracts() {
        check(ServiceConfigurationError.class.getSuperclass() == Error.class,
              "extends Error (unchecked, not Exception)");
        check(!Exception.class.isAssignableFrom(ServiceConfigurationError.class),
              "not an Exception subtype");
        ServiceConfigurationError e = new ServiceConfigurationError("boom");
        check("boom".equals(e.getMessage()), "message ctor");
        check(e.getCause() == null, "no cause by default");
        IllegalStateException cause = new IllegalStateException("root");
        ServiceConfigurationError e2 = new ServiceConfigurationError("msg", cause);
        check("msg".equals(e2.getMessage()) && e2.getCause() == cause,
              "2-arg ctor message + cause");
        check(e.toString().startsWith("java.util.ServiceConfigurationError"),
              "toString names the class");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSceContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
