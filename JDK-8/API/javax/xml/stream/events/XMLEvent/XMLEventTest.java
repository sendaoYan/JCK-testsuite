/*
 * @test
 * @summary javax.xml.stream.events.XMLEvent conformance: interface hierarchy and methods.
 * @run main XMLEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLEventTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.XMLEvent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.XMLStreamConstants"), "implements javax.xml.stream.XMLStreamConstants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getEventType"), "has method getEventType");
        check(names.contains("getLocation"), "has method getLocation");
        check(names.contains("isStartElement"), "has method isStartElement");
        check(names.contains("isAttribute"), "has method isAttribute");
        check(names.contains("isNamespace"), "has method isNamespace");
        check(names.contains("isEndElement"), "has method isEndElement");
        check(names.contains("isEntityReference"), "has method isEntityReference");
        check(names.contains("isProcessingInstruction"), "has method isProcessingInstruction");
        check(names.contains("isCharacters"), "has method isCharacters");
        check(names.contains("isStartDocument"), "has method isStartDocument");
        check(names.contains("isEndDocument"), "has method isEndDocument");
        check(names.contains("asStartElement"), "has method asStartElement");
        check(names.contains("asEndElement"), "has method asEndElement");
        check(names.contains("asCharacters"), "has method asCharacters");
        check(names.contains("getSchemaType"), "has method getSchemaType");
        check(names.contains("writeAsEncodedUnicode"), "has method writeAsEncodedUnicode");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (real events via XMLEventFactory + XMLEventReader) ----
    static void testDeep() throws Exception {
        // 1) event-code family: XMLStreamConstants exact values
        check(javax.xml.stream.XMLStreamConstants.START_ELEMENT == 1, "START_ELEMENT=1");
        check(javax.xml.stream.XMLStreamConstants.END_ELEMENT == 2, "END_ELEMENT=2");
        check(javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION == 3, "PROCESSING_INSTRUCTION=3");
        check(javax.xml.stream.XMLStreamConstants.CHARACTERS == 4, "CHARACTERS=4");
        check(javax.xml.stream.XMLStreamConstants.COMMENT == 5, "COMMENT=5");
        check(javax.xml.stream.XMLStreamConstants.SPACE == 6, "SPACE=6");
        check(javax.xml.stream.XMLStreamConstants.START_DOCUMENT == 7, "START_DOCUMENT=7");
        check(javax.xml.stream.XMLStreamConstants.END_DOCUMENT == 8, "END_DOCUMENT=8");
        check(javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE == 9, "ENTITY_REFERENCE=9");
        check(javax.xml.stream.XMLStreamConstants.ATTRIBUTE == 10, "ATTRIBUTE=10");
        check(javax.xml.stream.XMLStreamConstants.DTD == 11, "DTD=11");
        check(javax.xml.stream.XMLStreamConstants.CDATA == 12, "CDATA=12");
        check(javax.xml.stream.XMLStreamConstants.NAMESPACE == 13, "NAMESPACE=13");
        check(javax.xml.stream.XMLStreamConstants.NOTATION_DECLARATION == 14, "NOTATION_DECLARATION=14");
        check(javax.xml.stream.XMLStreamConstants.ENTITY_DECLARATION == 15, "ENTITY_DECLARATION=15");

        // 2) walk a fixed document: per-event type code + isXxx family + asXxx identity
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(new java.io.StringReader("<r>ab</r>"));
        javax.xml.stream.events.XMLEvent sd = r.nextEvent();
        check(sd.getEventType() == 7, "SD code 7");
        check(sd.isStartDocument() && !sd.isStartElement() && !sd.isEndElement() && !sd.isCharacters()
                && !sd.isAttribute() && !sd.isNamespace() && !sd.isEntityReference() && !sd.isProcessingInstruction() && !sd.isEndDocument(),
                "SD isXxx family exactly one true");
        check(sd.getLocation().getLineNumber() == 1 && sd.getLocation().getColumnNumber() == 1
                && sd.getLocation().getCharacterOffset() == 0, "SD location (1,1,0)");
        javax.xml.stream.events.XMLEvent se = r.nextEvent();
        check(se.getEventType() == 1 && se.isStartElement() && se.asStartElement() == se, "SE code + self cast");
        javax.xml.stream.events.XMLEvent ch = r.nextEvent();
        check(ch.getEventType() == 4 && ch.isCharacters() && ch.asCharacters() == ch, "CH code + self cast");
        javax.xml.stream.events.XMLEvent ee = r.nextEvent();
        check(ee.getEventType() == 2 && ee.isEndElement() && ee.asEndElement() == ee, "EE code + self cast");
        javax.xml.stream.events.XMLEvent ed = r.nextEvent();
        check(ed.getEventType() == 8 && ed.isEndDocument(), "ED code 8");
        check(ed.getLocation().getLineNumber() == -1 && ed.getLocation().getColumnNumber() == -1
                && ed.getLocation().getCharacterOffset() == -1, "ED location all -1");
        check(!r.hasNext(), "reader exhausted");
        try { r.nextEvent(); check(false, "nextEvent at end must throw"); }
        catch (java.util.NoSuchElementException nsee) { passed++; }
        r.close();

        // 3) ClassCastException matrix on wrong casts
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        javax.xml.stream.events.Characters cc = ef.createCharacters("x");
        try { cc.asStartElement(); check(false, "Characters.asStartElement must CCE"); }
        catch (ClassCastException e) { passed++; }
        try { cc.asEndElement(); check(false, "Characters.asEndElement must CCE"); }
        catch (ClassCastException e) { passed++; }
        javax.xml.stream.events.StartElement s2 = ef.createStartElement("p", "http://p", "l");
        try { s2.asCharacters(); check(false, "StartElement.asCharacters must CCE"); }
        catch (ClassCastException e) { passed++; }
        check(s2.asStartElement() == s2, "factory StartElement asStartElement self");
        check(s2.getEventType() == 1 && s2.isStartElement(), "factory StartElement code 1");

        // 4) writeAsEncodedUnicode: escaping of <&>"' (single quote kept raw), null Writer -> NPE
        java.io.StringWriter w = new java.io.StringWriter();
        ef.createCharacters("<&>\"'").writeAsEncodedUnicode(w);
        check("&lt;&amp;&gt;&quot;'".equals(w.toString()), "encoded escaping <&>\"' -> &lt;&amp;&gt;&quot;'");
        try { ef.createCharacters("x").writeAsEncodedUnicode(null); check(false, "null Writer must NPE"); }
        catch (NullPointerException npe) { passed++; }

        // 5) getSchemaType is null for non-schema events
        check(ef.createCharacters("x").getSchemaType() == null, "Characters getSchemaType null");
        check(ef.createStartElement("l", "", "").getSchemaType() == null, "StartElement getSchemaType null");

        // 6) factory default Location is a non-null dummy with -1/-1/-1 and null ids
        javax.xml.stream.Location dl = ef.createCharacters("x").getLocation();
        check(dl != null && dl.getLineNumber() == -1 && dl.getColumnNumber() == -1
                && dl.getCharacterOffset() == -1 && dl.getPublicId() == null && dl.getSystemId() == null,
                "factory default dummy location");

        // 7) setLocation wiring is copied into every created event
        javax.xml.stream.Location fix = new javax.xml.stream.Location() {
            public int getLineNumber() { return 10; }
            public int getColumnNumber() { return 20; }
            public int getCharacterOffset() { return 30; }
            public String getPublicId() { return "pub"; }
            public String getSystemId() { return "sys"; }
        };
        ef.setLocation(fix);
        javax.xml.stream.Location fl = ef.createCharacters("x").getLocation();
        check(fl.getLineNumber() == 10 && fl.getColumnNumber() == 20 && fl.getCharacterOffset() == 30
                && "pub".equals(fl.getPublicId()) && "sys".equals(fl.getSystemId()), "setLocation wired");
        ef.setLocation(null);
        check(ef.createCharacters("y").getLocation() != null, "setLocation(null) still non-null location");

        // 8) peek does not consume: peek twice then next yields the same event type
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(new java.io.StringReader("<r/>"));
        check(r2.peek().getEventType() == 7 && r2.peek().getEventType() == 7 && r2.nextEvent().getEventType() == 7,
                "peek twice non-consuming");
        while (r2.hasNext()) r2.nextEvent();
        check(r2.peek() == null, "peek at end null");
        r2.close();

        // 9) DTD and ENTITY_REFERENCE events carry their spec codes (11 and 9)
        javax.xml.stream.XMLInputFactory nf = javax.xml.stream.XMLInputFactory.newFactory();
        nf.setProperty(javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        javax.xml.stream.XMLEventReader r3 = nf.createXMLEventReader(
                new java.io.StringReader("<!DOCTYPE r [<!ENTITY e 'H'>]><r>&e;</r>"));
        boolean sawDtd = false, sawEr = false;
        while (r3.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r3.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) {
                sawDtd = true;
                check(e.getEventType() == 11, "DTD code 11");
            }
            if (e.isEntityReference()) {
                sawEr = true;
                check(e.getEventType() == 9, "ENTITY_REFERENCE code 9");
                try { e.asStartElement(); check(false, "ER.asStartElement must CCE"); }
                catch (ClassCastException cce) { passed++; }
            }
        }
        check(sawDtd && sawEr, "DTD and ER events both observed");
        r3.close();
    }
}
