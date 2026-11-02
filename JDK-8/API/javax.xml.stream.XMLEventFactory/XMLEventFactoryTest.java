/*
 * @test
 * @summary javax.xml.stream.XMLEventFactory conformance: XML event factory.
 * @run main XMLEventFactoryTest
 */


public class XMLEventFactoryTest {
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
        javax.xml.stream.XMLEventFactory f = javax.xml.stream.XMLEventFactory.newFactory();
        check(f != null, "factory non-null");
        javax.xml.stream.events.StartElement se = f.createStartElement("", "", "a");
        check("a".equals(se.getName().getLocalPart()), "start element name");
        javax.xml.stream.events.EndElement ee = f.createEndElement("", "", "a");
        check("a".equals(ee.getName().getLocalPart()), "end element name");
        javax.xml.stream.events.Characters ch = f.createCharacters("text");
        check("text".equals(ch.getData()), "characters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
