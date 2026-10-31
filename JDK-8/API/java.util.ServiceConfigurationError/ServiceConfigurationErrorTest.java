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

    public static void main(String[] args) {
        testServiceConfigurationError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
