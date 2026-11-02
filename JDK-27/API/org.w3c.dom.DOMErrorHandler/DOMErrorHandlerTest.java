/*
 * @test
 * @summary org.w3c.dom.DOMErrorHandler conformance: interface and handleError.
 * @run main DOMErrorHandlerTest
 */

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

public class DOMErrorHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DOMErrorHandler.class.isInterface(), "DOMErrorHandler is an interface");
        check(DOMErrorHandler.class.getMethod("handleError", DOMError.class)
                .getReturnType() == boolean.class, "handleError(DOMError) returns boolean");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
