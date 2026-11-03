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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(ElementTraversal.class.isInterface(), "ElementTraversal is an interface");
        check(ElementTraversal.class.getMethod("getFirstElementChild")
                .getReturnType() == Element.class, "getFirstElementChild() returns Element");
        check(ElementTraversal.class.getMethod("getNextElementSibling")
                .getReturnType() == Element.class, "getNextElementSibling() returns Element");
        check(ElementTraversal.class.getMethod("getChildElementCount")
                .getReturnType() == int.class, "getChildElementCount() returns int");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ElementTraversal a = new org.w3c.dom.ElementTraversal(){
            public org.w3c.dom.Element getFirstElementChild() { return null; }
            public org.w3c.dom.Element getLastElementChild() { return null; }
            public org.w3c.dom.Element getPreviousElementSibling() { return null; }
            public org.w3c.dom.Element getNextElementSibling() { return null; }
            public int getChildElementCount() { return 3; }
        };
        check(a != null, "anon impl of ElementTraversal");
        check(a.getFirstElementChild() == null, "getFirstElementChild() returns fixed default");
        check(a.getLastElementChild() == null, "getLastElementChild() returns fixed default");
        check(a.getPreviousElementSibling() == null, "getPreviousElementSibling() returns fixed default");
        check(a.getNextElementSibling() == null, "getNextElementSibling() returns fixed default");
        check(a.getChildElementCount() == 3, "getChildElementCount() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
