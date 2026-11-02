/*
 * @test
 * @summary org.w3c.dom.DOMConfiguration conformance: interface and methods.
 * @run main DOMConfigurationTest
 */

import org.w3c.dom.DOMConfiguration;

public class DOMConfigurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DOMConfiguration.class.isInterface(), "DOMConfiguration is an interface");
        check(DOMConfiguration.class.getMethod("setParameter", String.class, Object.class)
                != null, "setParameter(String,Object)");
        check(DOMConfiguration.class.getMethod("getParameter", String.class) != null,
              "getParameter(String)");
        check(DOMConfiguration.class.getMethod("canSetParameter", String.class, Object.class)
                .getReturnType() == boolean.class, "canSetParameter returns boolean");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
