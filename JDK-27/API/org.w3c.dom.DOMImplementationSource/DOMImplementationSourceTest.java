/*
 * @test
 * @summary org.w3c.dom.DOMImplementationSource conformance: interface and methods.
 * @run main DOMImplementationSourceTest
 */

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.DOMImplementationSource;

public class DOMImplementationSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DOMImplementationSource.class.isInterface(),
              "DOMImplementationSource is an interface");
        check(DOMImplementationSource.class.getMethod("getDOMImplementation", String.class)
                .getReturnType() == DOMImplementation.class,
              "getDOMImplementation(String) returns DOMImplementation");
        check(DOMImplementationSource.class.getMethod("getDOMImplementationList", String.class)
                .getReturnType() == DOMImplementationList.class,
              "getDOMImplementationList(String) returns DOMImplementationList");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
