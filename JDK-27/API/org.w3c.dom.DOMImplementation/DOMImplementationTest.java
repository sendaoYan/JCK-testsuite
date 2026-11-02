/*
 * @test
 * @summary org.w3c.dom.DOMImplementation conformance: interface and methods.
 * @run main DOMImplementationTest
 */

import org.w3c.dom.DOMImplementation;

public class DOMImplementationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DOMImplementation.class.isInterface(), "DOMImplementation is an interface");
        check(DOMImplementation.class.getMethod("hasFeature", String.class, String.class)
                .getReturnType() == boolean.class, "hasFeature(String,String) returns boolean");
        check(DOMImplementation.class.getMethod("createDocumentType", String.class,
                String.class, String.class) != null, "createDocumentType(String,String,String)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
