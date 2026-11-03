/*
 * @test
 * @summary org.w3c.dom.views.AbstractView conformance: interface and getDocument.
 * @run main AbstractViewTest
 */

import org.w3c.dom.views.AbstractView;

public class AbstractViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(AbstractView.class.isInterface(), "AbstractView is an interface");
        check(AbstractView.class.getMethod("getDocument") != null, "getDocument()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.views.AbstractView a = new org.w3c.dom.views.AbstractView(){
            public org.w3c.dom.views.DocumentView getDocument() { return null; }
        };
        check(a != null, "anon impl of AbstractView");
        check(a.getDocument() == null, "getDocument() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
