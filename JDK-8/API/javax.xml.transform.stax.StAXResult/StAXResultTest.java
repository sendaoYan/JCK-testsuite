/*
 * @test
 * @summary javax.xml.transform.stax.StAXResult conformance: StAX result.
 * @run main StAXResultTest
 */


public class StAXResultTest {
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
        javax.xml.stream.XMLOutputFactory f = javax.xml.stream.XMLOutputFactory.newFactory();
        javax.xml.stream.XMLStreamWriter w = f.createXMLStreamWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r = new javax.xml.transform.stax.StAXResult(w);
        check(r.getXMLStreamWriter() == w, "stream writer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
