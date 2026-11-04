/*
 * @test
 * @summary java.util.ServiceConfigurationError API conformance: constructors and inheritance.
 * @run main ServiceConfigurationErrorTest
 */

import java.util.ServiceConfigurationError;

public class ServiceConfigurationErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testServiceConfigurationError() {
        ServiceConfigurationError e = new ServiceConfigurationError("msg");
        check(e instanceof Error, "extends Error");
        check("msg".equals(e.getMessage()), "message from (String) ctor");

        RuntimeException cause = new RuntimeException();
        ServiceConfigurationError e2 = new ServiceConfigurationError("m", cause);
        check(e2.getCause() == cause, "(String,Throwable) ctor sets cause");
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

    public static void main(String[] args) {
        testServiceConfigurationError();
        testSceContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
