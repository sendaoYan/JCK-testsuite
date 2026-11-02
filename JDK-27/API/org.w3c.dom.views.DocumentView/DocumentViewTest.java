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

    public static void main(String[] args) throws Exception {
        check(DocumentView.class.isInterface(), "DocumentView is an interface");
        check(DocumentView.class.getMethod("getDefaultView")
                .getReturnType() == AbstractView.class,
              "getDefaultView() returns AbstractView");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
