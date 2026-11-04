/*
 * @test
 * @summary javax.xml.XMLConstants conformance: XML constants.
 * @run main XMLConstantsTest
 */


public class XMLConstantsTest {
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
        check(javax.xml.XMLConstants.NULL_NS_URI.equals(""), "NULL_NS_URI");
        check(javax.xml.XMLConstants.XML_NS_URI.equals("http://www.w3.org/XML/1998/namespace"), "XML_NS_URI");
        check(javax.xml.XMLConstants.XMLNS_ATTRIBUTE.equals("xmlns"), "XMLNS_ATTRIBUTE");
        check(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI.equals("http://www.w3.org/2001/XMLSchema"), "W3C_XML_SCHEMA_NS_URI");
        check(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING.equals("http://javax.xml.XMLConstants/feature/secure-processing"), "FEATURE_SECURE_PROCESSING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
