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

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(XPathException.class), "XPathException extends RuntimeException");
        check(XPathException.INVALID_EXPRESSION_ERR == 1, "INVALID_EXPRESSION_ERR == 1");
        check(XPathException.TYPE_ERR == 2, "TYPE_ERR == 2");
        XPathException e = new XPathException(XPathException.INVALID_EXPRESSION_ERR, "boom");
        check(e.code == XPathException.INVALID_EXPRESSION_ERR, "code field");
        check("boom".equals(e.getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
