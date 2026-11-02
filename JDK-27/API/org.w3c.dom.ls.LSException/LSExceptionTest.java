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

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(LSException.class), "LSException extends RuntimeException");
        check(LSException.PARSE_ERR == 81, "PARSE_ERR == 81");
        check(LSException.SERIALIZE_ERR == 82, "SERIALIZE_ERR == 82");
        LSException e = new LSException(LSException.PARSE_ERR, "boom");
        check(e.code == LSException.PARSE_ERR, "code field");
        check("boom".equals(e.getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
