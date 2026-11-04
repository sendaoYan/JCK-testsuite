/*
 * @test
 * @summary javax.xml.stream.XMLInputFactory conformance: XML input factory.
 * @run main XMLInputFactoryTest
 */


public class XMLInputFactoryTest {
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
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        check(f != null, "factory non-null");
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader("<a b='1'/>"));
        check(r.getEventType() == javax.xml.stream.XMLStreamConstants.START_DOCUMENT, "initial START_DOCUMENT");
        r.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
