/*
 * @test
 * @summary org.w3c.dom.Element conformance: interface extends Node.
 * @run main ElementTest
 */

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Element.class.isInterface(), "Element is an interface");
        check(Node.class.isAssignableFrom(Element.class), "extends Node");
        check(Element.class.getMethod("getTagName") != null, "getTagName()");
        check(Element.class.getMethod("getAttribute", String.class) != null,
              "getAttribute(String)");
        check(Element.class.getMethod("getElementsByTagName", String.class) != null,
              "getElementsByTagName(String)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
