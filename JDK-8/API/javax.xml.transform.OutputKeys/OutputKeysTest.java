/*
 * @test
 * @summary javax.xml.transform.OutputKeys conformance: output keys constants.
 * @run main OutputKeysTest
 */


public class OutputKeysTest {
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
        check("method".equals(javax.xml.transform.OutputKeys.METHOD), "METHOD");
        check("version".equals(javax.xml.transform.OutputKeys.VERSION), "VERSION");
        check("encoding".equals(javax.xml.transform.OutputKeys.ENCODING), "ENCODING");
        check("omit-xml-declaration".equals(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION), "OMIT_XML_DECLARATION");
        check("indent".equals(javax.xml.transform.OutputKeys.INDENT), "INDENT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
