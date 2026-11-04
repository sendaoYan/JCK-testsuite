/*
 * @test
 * @summary javax.xml.transform.sax.SAXTransformerFactory conformance: SAX transformer factory.
 * @run main SAXTransformerFactoryTest
 */


public class SAXTransformerFactoryTest {
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
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        check(tf instanceof javax.xml.transform.sax.SAXTransformerFactory, "is SAXTransformerFactory");
        javax.xml.transform.sax.SAXTransformerFactory stf = (javax.xml.transform.sax.SAXTransformerFactory) tf;
        javax.xml.transform.sax.TransformerHandler th = stf.newTransformerHandler();
        check(th != null, "transformer handler non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
