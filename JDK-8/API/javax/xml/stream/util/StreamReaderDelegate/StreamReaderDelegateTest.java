/*
 * @test
 * @summary javax.xml.stream.util.StreamReaderDelegate conformance: stream reader delegate.
 * @run main StreamReaderDelegateTest
 */


public class StreamReaderDelegateTest {
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
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader("<a/>"));
        javax.xml.stream.util.StreamReaderDelegate d = new javax.xml.stream.util.StreamReaderDelegate(r);
        check(d.getParent() == r, "parent");
        check(d.hasNext(), "hasNext");
        d.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
