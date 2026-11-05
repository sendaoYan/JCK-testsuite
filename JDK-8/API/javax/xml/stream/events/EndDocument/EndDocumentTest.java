/*
 * @test
 * @summary javax.xml.stream.events.EndDocument conformance: interface hierarchy and methods.
 * @run main EndDocumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class EndDocumentTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.EndDocument");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (parsed final event + factory form) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) parsed EndDocument is the last event: code 8, family, dummy location -1/-1/-1
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<r>a</r>"));
        javax.xml.stream.events.XMLEvent last = null;
        while (r.hasNext()) last = r.nextEvent();
        check(last != null && last.getEventType() == 8 && last.isEndDocument(), "last event code 8");
        check(!last.isStartDocument() && !last.isStartElement() && !last.isEndElement() && !last.isCharacters()
                && !last.isProcessingInstruction() && !last.isEntityReference() && !last.isAttribute()
                && !last.isNamespace(), "ED isXxx family exactly one true");
        check(last.getLocation().getLineNumber() == -1 && last.getLocation().getColumnNumber() == -1
                && last.getLocation().getCharacterOffset() == -1, "ED location all -1");
        check(last.getSchemaType() == null, "ED getSchemaType null");
        r.close();

        // 2) factory createEndDocument: code 8, empty encoded output
        javax.xml.stream.events.EndDocument fed = ef.createEndDocument();
        check(fed.getEventType() == 8 && fed.isEndDocument(), "factory ED code 8");
        java.io.StringWriter w = new java.io.StringWriter();
        fed.writeAsEncodedUnicode(w);
        check("".equals(w.toString()), "factory ED encodes to empty string");
        try { fed.asCharacters(); check(false, "ED.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }
        try { fed.asStartElement(); check(false, "ED.asStartElement must CCE"); }
        catch (ClassCastException cce) { passed++; }
        try { fed.asEndElement(); check(false, "ED.asEndElement must CCE"); }
        catch (ClassCastException cce) { passed++; }

        // 3) writer round-trip: doc with factory ED event is well-formed and reparses
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos);
        ew.add(ef.createStartDocument("UTF-8", "1.0"));
        ew.add(ef.createStartElement("", "", "r"));
        ew.add(ef.createCharacters("v"));
        ew.add(ef.createEndElement("", "", "r"));
        ew.add(ef.createEndDocument());
        ew.flush();
        javax.xml.stream.XMLEventReader rr = ifac.createXMLEventReader(
                new java.io.StringReader(new String(bos.toByteArray(), "UTF-8")));
        javax.xml.stream.events.XMLEvent re = null;
        while (rr.hasNext()) re = rr.nextEvent();
        check(re != null && re.getEventType() == 8, "reparse ends with END_DOCUMENT");
        rr.close();
        ew.close();
    }
}
