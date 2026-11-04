/*
 * @test
 * @summary org.w3c.dom.DOMException conformance: superclass, constructor, code field.
 * @run main DOMExceptionTest
 */

import org.w3c.dom.DOMException;

public class DOMExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(DOMException.class),
              "extends RuntimeException");
        check(DOMException.INDEX_SIZE_ERR == 1, "INDEX_SIZE_ERR == 1");
        check(DOMException.WRONG_DOCUMENT_ERR == 4, "WRONG_DOCUMENT_ERR == 4");

        DOMException e = new DOMException(DOMException.NAMESPACE_ERR, "ns");
        check(e.code == DOMException.NAMESPACE_ERR, "code field");
        check("ns".equals(e.getMessage()), "message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
