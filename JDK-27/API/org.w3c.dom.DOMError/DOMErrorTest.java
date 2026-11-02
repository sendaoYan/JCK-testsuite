/*
 * @test
 * @summary org.w3c.dom.DOMError conformance: interface and methods.
 * @run main DOMErrorTest
 */

import org.w3c.dom.DOMError;

public class DOMErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DOMError.class.isInterface(), "DOMError is an interface");
        check(DOMError.class.getMethod("getSeverity") != null, "getSeverity()");
        check(DOMError.class.getMethod("getMessage") != null, "getMessage()");
        check(DOMError.class.getMethod("getType") != null, "getType()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
