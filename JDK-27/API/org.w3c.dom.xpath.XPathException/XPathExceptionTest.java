/*
 * @test
 * @summary org.w3c.dom.xpath.XPathException conformance: superclass, constructor, code field.
 * @run main XPathExceptionTest
 */

import org.w3c.dom.xpath.XPathException;

public class XPathExceptionTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(RuntimeException.class.isAssignableFrom(XPathException.class), "XPathException extends RuntimeException");
        check(XPathException.INVALID_EXPRESSION_ERR == 1, "INVALID_EXPRESSION_ERR == 1");
        check(XPathException.TYPE_ERR == 2, "TYPE_ERR == 2");
        XPathException e = new XPathException(XPathException.INVALID_EXPRESSION_ERR, "boom");
        check(e.code == XPathException.INVALID_EXPRESSION_ERR, "code field");
        check("boom".equals(e.getMessage()), "message");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(XPathException.INVALID_EXPRESSION_ERR == 1, "INVALID_EXPRESSION_ERR == 1");
        check(XPathException.TYPE_ERR == 2, "TYPE_ERR == 2");
        org.w3c.dom.xpath.XPathException e = new org.w3c.dom.xpath.XPathException((short) 3, "msg");
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
