/*
 * @test
 * @summary javax.xml.stream.XMLOutputFactory conformance: XML output factory.
 * @run main XMLOutputFactoryTest
 */


public class XMLOutputFactoryTest {
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
        check(f != null, "factory non-null");
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = f.createXMLStreamWriter(sw);
        w.writeStartDocument();
        w.writeStartElement("a");
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        check(sw.toString().indexOf("<a") >= 0, "wrote element");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
