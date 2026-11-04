/*
 * @test
 * @summary javax.xml.transform.stax.StAXSource conformance: StAX source.
 * @run main StAXSourceTest
 */


public class StAXSourceTest {
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
        javax.xml.stream.XMLStreamReader rdr = f.createXMLStreamReader(new java.io.StringReader("<a/>"));
        javax.xml.transform.stax.StAXSource s = new javax.xml.transform.stax.StAXSource(rdr);
        check(s.getXMLStreamReader() == rdr, "stream reader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
