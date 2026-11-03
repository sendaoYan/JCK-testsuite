/*
 * @test
 * @summary org.w3c.dom.views.DocumentView conformance: interface and getDefaultView.
 * @run main DocumentViewTest
 */

import org.w3c.dom.views.AbstractView;
import org.w3c.dom.views.DocumentView;

public class DocumentViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentView.class.isInterface(), "DocumentView is an interface");
        check(DocumentView.class.getMethod("getDefaultView")
                .getReturnType() == AbstractView.class,
              "getDefaultView() returns AbstractView");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.views.DocumentView a = new org.w3c.dom.views.DocumentView(){
            public org.w3c.dom.views.AbstractView getDefaultView() { return null; }
        };
        check(a != null, "anon impl of DocumentView");
        check(a.getDefaultView() == null, "getDefaultView() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
