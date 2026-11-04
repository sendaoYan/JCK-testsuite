/*
 * @test
 * @summary javax.xml.parsers.DocumentBuilderFactory conformance: document builder factory.
 * @run main DocumentBuilderFactoryTest
 */


public class DocumentBuilderFactoryTest {
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
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        check(dbf != null, "factory non-null");
        dbf.setNamespaceAware(true);
        check(dbf.isNamespaceAware(), "namespace aware");
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        check(db != null, "builder non-null");
        org.w3c.dom.Document doc = db.parse(new org.xml.sax.InputSource(new java.io.StringReader("<a/>")));
        check("a".equals(doc.getDocumentElement().getTagName()), "parsed root");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
