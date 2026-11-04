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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMErrorHandler.class.isInterface(), "DOMErrorHandler is an interface");
        check(DOMErrorHandler.class.getMethod("handleError", DOMError.class)
                .getReturnType() == boolean.class, "handleError(DOMError) returns boolean");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMErrorHandler a = new org.w3c.dom.DOMErrorHandler(){
            public boolean handleError(org.w3c.dom.DOMError p0) { return true; }
        };
        check(a != null, "anon impl of DOMErrorHandler");
        check(a.handleError((org.w3c.dom.DOMError) null) == true, "handleError() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
