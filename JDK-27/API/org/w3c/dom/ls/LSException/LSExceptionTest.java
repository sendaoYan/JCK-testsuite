/*
 * @test
 * @summary org.w3c.dom.ls.LSException conformance: superclass, constructor, code field.
 * @run main LSExceptionTest
 */

import org.w3c.dom.ls.LSException;

public class LSExceptionTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(RuntimeException.class.isAssignableFrom(LSException.class), "LSException extends RuntimeException");
        check(LSException.PARSE_ERR == 81, "PARSE_ERR == 81");
        check(LSException.SERIALIZE_ERR == 82, "SERIALIZE_ERR == 82");
        LSException e = new LSException(LSException.PARSE_ERR, "boom");
        check(e.code == LSException.PARSE_ERR, "code field");
        check("boom".equals(e.getMessage()), "message");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(LSException.PARSE_ERR == 81, "PARSE_ERR == 81");
        check(LSException.SERIALIZE_ERR == 82, "SERIALIZE_ERR == 82");
        org.w3c.dom.ls.LSException e = new org.w3c.dom.ls.LSException((short) 3, "msg");
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
