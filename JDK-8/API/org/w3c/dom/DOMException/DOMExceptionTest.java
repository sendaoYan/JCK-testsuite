/*
 * @test
 * @summary org.w3c.dom.DOMException conformance: exception hierarchy, constructor, code field, full error-code constant matrix.
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
        // ---- exception hierarchy ----
        check(RuntimeException.class.isAssignableFrom(DOMException.class), "extends RuntimeException");
        check(Exception.class.isAssignableFrom(DOMException.class), "is an Exception");

        // ---- full error-code constant matrix (DOM Level 3) ----
        check(DOMException.INDEX_SIZE_ERR == 1, "INDEX_SIZE_ERR == 1");
        check(DOMException.DOMSTRING_SIZE_ERR == 2, "DOMSTRING_SIZE_ERR == 2");
        check(DOMException.HIERARCHY_REQUEST_ERR == 3, "HIERARCHY_REQUEST_ERR == 3");
        check(DOMException.WRONG_DOCUMENT_ERR == 4, "WRONG_DOCUMENT_ERR == 4");
        check(DOMException.INVALID_CHARACTER_ERR == 5, "INVALID_CHARACTER_ERR == 5");
        check(DOMException.NO_DATA_ALLOWED_ERR == 6, "NO_DATA_ALLOWED_ERR == 6");
        check(DOMException.NO_MODIFICATION_ALLOWED_ERR == 7, "NO_MODIFICATION_ALLOWED_ERR == 7");
        check(DOMException.NOT_FOUND_ERR == 8, "NOT_FOUND_ERR == 8");
        check(DOMException.NOT_SUPPORTED_ERR == 9, "NOT_SUPPORTED_ERR == 9");
        check(DOMException.INUSE_ATTRIBUTE_ERR == 10, "INUSE_ATTRIBUTE_ERR == 10");
        check(DOMException.INVALID_STATE_ERR == 11, "INVALID_STATE_ERR == 11");
        check(DOMException.SYNTAX_ERR == 12, "SYNTAX_ERR == 12");
        check(DOMException.INVALID_MODIFICATION_ERR == 13, "INVALID_MODIFICATION_ERR == 13");
        check(DOMException.NAMESPACE_ERR == 14, "NAMESPACE_ERR == 14");
        check(DOMException.INVALID_ACCESS_ERR == 15, "INVALID_ACCESS_ERR == 15");
        check(DOMException.VALIDATION_ERR == 16, "VALIDATION_ERR == 16");
        check(DOMException.TYPE_MISMATCH_ERR == 17, "TYPE_MISMATCH_ERR == 17");

        // ---- constructor + public code field ----
        DOMException e = new DOMException(DOMException.NAMESPACE_ERR, "ns message");
        check(e.code == DOMException.NAMESPACE_ERR, "code field populated from constructor");
        check("ns message".equals(e.getMessage()), "message preserved");
        check(e.getCause() == null, "no cause by default");

        DOMException e2 = new DOMException(DOMException.NOT_FOUND_ERR, null);
        check(e2.code == DOMException.NOT_FOUND_ERR, "code with null message");
        check(e2.getMessage() == null, "null message preserved");

        // ---- code is a mutable public field ----
        e2.code = DOMException.WRONG_DOCUMENT_ERR;
        check(e2.code == DOMException.WRONG_DOCUMENT_ERR, "code field is public and mutable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
