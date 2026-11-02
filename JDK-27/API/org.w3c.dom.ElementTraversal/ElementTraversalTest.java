/*
 * @test
 * @summary org.w3c.dom.ElementTraversal conformance: interface and methods.
 * @run main ElementTraversalTest
 */

import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;

public class ElementTraversalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(ElementTraversal.class.isInterface(), "ElementTraversal is an interface");
        check(ElementTraversal.class.getMethod("getFirstElementChild")
                .getReturnType() == Element.class, "getFirstElementChild() returns Element");
        check(ElementTraversal.class.getMethod("getNextElementSibling")
                .getReturnType() == Element.class, "getNextElementSibling() returns Element");
        check(ElementTraversal.class.getMethod("getChildElementCount")
                .getReturnType() == int.class, "getChildElementCount() returns int");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
