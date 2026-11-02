/*
 * @test
 * @summary org.w3c.dom.DOMImplementationList conformance: interface and methods.
 * @run main DOMImplementationListTest
 */

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;

public class DOMImplementationListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DOMImplementationList.class.isInterface(),
              "DOMImplementationList is an interface");
        check(DOMImplementationList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(DOMImplementationList.class.getMethod("item", int.class)
                .getReturnType() == DOMImplementation.class, "item(int) returns DOMImplementation");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
