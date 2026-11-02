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

    public static void main(String[] args) throws Exception {
        check(AbstractView.class.isInterface(), "AbstractView is an interface");
        check(AbstractView.class.getMethod("getDocument") != null, "getDocument()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
