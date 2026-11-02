/*
 * @test
 * @summary org.w3c.dom.Document conformance: interface extends Node.
 * @run main DocumentTest
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DocumentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Document.class.isInterface(), "Document is an interface");
        check(Node.class.isAssignableFrom(Document.class), "extends Node");
        check(Document.class.getMethod("getDocumentElement") != null,
              "getDocumentElement()");
        check(Document.class.getMethod("createElement", String.class)
                .getReturnType() == Element.class, "createElement(String) returns Element");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
