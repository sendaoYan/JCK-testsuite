/*
 * @test
 * @summary org.w3c.dom.DOMLocator conformance: interface and methods.
 * @run main DOMLocatorTest
 */

import org.w3c.dom.DOMLocator;

public class DOMLocatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DOMLocator.class.isInterface(), "DOMLocator is an interface");
        check(DOMLocator.class.getMethod("getLineNumber").getReturnType() == int.class,
              "getLineNumber() returns int");
        check(DOMLocator.class.getMethod("getColumnNumber").getReturnType() == int.class,
              "getColumnNumber() returns int");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
