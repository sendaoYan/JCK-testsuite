/*
 * @test
 * @summary org.xml.sax.SAXParseException conformance: exception hierarchy and constructors.
 * @run main SAXParseExceptionTest
 */


public class SAXParseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(org.xml.sax.SAXParseException.class.getSuperclass().getName().equals("org.xml.sax.SAXException"), "extends org.xml.sax.SAXException");
        org.xml.sax.SAXParseException pe = new org.xml.sax.SAXParseException("msg", "pub", "sys", 12, 34);
        check(pe.getLineNumber() == 12, "line number");
        check(pe.getColumnNumber() == 34, "column number");
        check("pub".equals(pe.getPublicId()), "public id");
        check("sys".equals(pe.getSystemId()), "system id");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
