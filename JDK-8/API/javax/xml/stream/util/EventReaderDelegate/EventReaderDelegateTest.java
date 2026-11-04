/*
 * @test
 * @summary javax.xml.stream.util.EventReaderDelegate conformance: event reader delegate.
 * @run main EventReaderDelegateTest
 */


public class EventReaderDelegateTest {
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
        javax.xml.stream.XMLEventReader rdr = f.createXMLEventReader(new java.io.StringReader("<a/>"));
        javax.xml.stream.util.EventReaderDelegate d = new javax.xml.stream.util.EventReaderDelegate();
        check(d.getParent() == null, "parent null");
        d.setParent(rdr);
        check(d.getParent() == rdr, "parent set");
        check(d.hasNext(), "hasNext");
        d.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
