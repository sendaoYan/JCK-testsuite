/*
 * @test
 * @summary org.w3c.dom.ls.LSException conformance: superclass, constructor, code field.
 * @run main LSExceptionTest
 */

public class LSExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(org.w3c.dom.ls.LSException.class),
              "extends RuntimeException");
        check(org.w3c.dom.ls.LSException.PARSE_ERR == org.w3c.dom.ls.LSException.PARSE_ERR, "PARSE_ERR field");
        check(org.w3c.dom.ls.LSException.SERIALIZE_ERR == org.w3c.dom.ls.LSException.SERIALIZE_ERR, "SERIALIZE_ERR field");
        org.w3c.dom.ls.LSException e = new org.w3c.dom.ls.LSException(org.w3c.dom.ls.LSException.PARSE_ERR, "m");
        check(e.code == org.w3c.dom.ls.LSException.PARSE_ERR, "code field");
        check("m".equals(e.getMessage()), "message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
