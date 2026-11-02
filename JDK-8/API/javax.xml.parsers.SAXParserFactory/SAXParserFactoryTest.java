/*
 * @test
 * @summary javax.xml.parsers.SAXParserFactory conformance: SAX parser factory.
 * @run main SAXParserFactoryTest
 */


public class SAXParserFactoryTest {
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
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        check(spf != null, "factory non-null");
        spf.setNamespaceAware(true);
        check(spf.isNamespaceAware(), "namespace aware");
        javax.xml.parsers.SAXParser sp = spf.newSAXParser();
        check(sp != null, "parser non-null");
        check(sp.isNamespaceAware(), "parser namespace aware");
        check(sp.getXMLReader() != null, "xml reader non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
