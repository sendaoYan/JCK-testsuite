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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(RuntimeException.class.isAssignableFrom(DOMException.class),
              "extends RuntimeException");
        check(DOMException.INDEX_SIZE_ERR == 1, "INDEX_SIZE_ERR == 1");
        check(DOMException.WRONG_DOCUMENT_ERR == 4, "WRONG_DOCUMENT_ERR == 4");

        DOMException e = new DOMException(DOMException.NAMESPACE_ERR, "ns");
        check(e.code == DOMException.NAMESPACE_ERR, "code field");
        check("ns".equals(e.getMessage()), "message");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
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
        org.w3c.dom.DOMException e = new org.w3c.dom.DOMException((short) 3, "msg");
        check(e.code == (short) 3, "code field set");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof RuntimeException, "is RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
