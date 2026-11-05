/*
 * @test
 * @summary javax.xml.stream.events.ProcessingInstruction conformance: interface hierarchy and methods.
 * @run main ProcessingInstructionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ProcessingInstructionTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.ProcessingInstruction");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getTarget"), "has method getTarget");
        check(names.contains("getData"), "has method getData");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (factory + parsed round-trips) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory createProcessingInstruction: target/data equality, code, family, encoded
        javax.xml.stream.events.ProcessingInstruction pi = ef.createProcessingInstruction("t", "d");
        check("t".equals(pi.getTarget()) && "d".equals(pi.getData()), "factory PI target/data");
        check(pi.getEventType() == 3, "PI code 3");
        check(pi.isProcessingInstruction() && !pi.isCharacters() && !pi.isStartElement() && !pi.isEndElement(),
                "PI isXxx family");
        java.io.StringWriter w = new java.io.StringWriter();
        pi.writeAsEncodedUnicode(w);
        check("<?t d?>".equals(w.toString()), "PI encoded <?t d?>");
        // null data tolerated; encoded drops the data part
        javax.xml.stream.events.ProcessingInstruction pin = ef.createProcessingInstruction("t", null);
        check(pin.getData() == null && "t".equals(pin.getTarget()), "factory PI null data");
        java.io.StringWriter wn = new java.io.StringWriter();
        pin.writeAsEncodedUnicode(wn);
        check("<?t?>".equals(wn.toString()), "PI null-data encoded <?t?>");
        // null target tolerated by factory
        javax.xml.stream.events.ProcessingInstruction pit = ef.createProcessingInstruction(null, "d");
        check(pit.getTarget() == null, "factory PI null target");
        try { pi.asCharacters(); check(false, "PI.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }

        // 2) parsed PI in prolog and inside element: target/data exact
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<?pi1 data1?><r><?in mid?></r>"));
        r.nextEvent(); // START_DOCUMENT always first
        javax.xml.stream.events.ProcessingInstruction p1 = (javax.xml.stream.events.ProcessingInstruction) r.nextEvent();
        check("pi1".equals(p1.getTarget()) && "data1".equals(p1.getData()), "prolog PI target/data");
        check(p1.isProcessingInstruction(), "prolog PI isProcessingInstruction");
        r.nextEvent();
        javax.xml.stream.events.ProcessingInstruction p2 = (javax.xml.stream.events.ProcessingInstruction) r.nextEvent();
        check("in".equals(p2.getTarget()) && "mid".equals(p2.getData()), "content PI target/data");
        r.close();

        // 3) parsed PI without data: getData() is empty string (factory null differs)
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<r><?tgt?></r>"));
        r2.nextEvent(); r2.nextEvent();
        javax.xml.stream.events.ProcessingInstruction p3 = (javax.xml.stream.events.ProcessingInstruction) r2.nextEvent();
        check("tgt".equals(p3.getTarget()) && "".equals(p3.getData()), "no-data PI getData empty string");
        r2.close();

        // 4) round-trip: write factory PI into writer, reparse via event reader, values identical
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos);
        ew.add(ef.createStartDocument("UTF-8", "1.0"));
        ew.add(ef.createStartElement("", "", "r"));
        ew.add(ef.createProcessingInstruction("xsl", "transform"));
        ew.add(ef.createEndElement("", "", "r"));
        ew.add(ef.createEndDocument());
        ew.flush();
        javax.xml.stream.XMLEventReader rr = ifac.createXMLEventReader(
                new java.io.StringReader(new String(bos.toByteArray(), "UTF-8")));
        boolean found = false;
        while (rr.hasNext()) {
            javax.xml.stream.events.XMLEvent e = rr.nextEvent();
            if (e.getEventType() == 3) {
                javax.xml.stream.events.ProcessingInstruction px = (javax.xml.stream.events.ProcessingInstruction) e;
                check("xsl".equals(px.getTarget()) && "transform".equals(px.getData()), "round-trip PI values");
                found = true;
            }
        }
        check(found, "round-trip PI observed");
        rr.close();
        ew.close();
    }
}
