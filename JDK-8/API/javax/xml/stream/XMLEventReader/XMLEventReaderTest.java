/*
 * @test
 * @summary javax.xml.stream.XMLEventReader conformance: interface hierarchy and methods.
 * @run main XMLEventReaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLEventReaderTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLEventReader");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.util.Iterator"), "implements java.util.Iterator");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("nextEvent"), "has method nextEvent");
        check(names.contains("hasNext"), "has method hasNext");
        check(names.contains("peek"), "has method peek");
        check(names.contains("getElementText"), "has method getElementText");
        check(names.contains("nextTag"), "has method nextTag");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("close"), "has method close");
        testEventSequence();
        testPeekAndIterate();
        testElementTextAndNextTag();
        testNegativePaths();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.stream.XMLEventReader er(String xml) throws Exception {
        return javax.xml.stream.XMLInputFactory.newFactory().createXMLEventReader(new java.io.StringReader(xml));
    }

    // ---- Deepened 1: nextEvent object model over a fixed document ----
    static void testEventSequence() throws Exception {
        javax.xml.stream.XMLEventReader r = er("<r x='1' y='2'>t<?pi d?><!--c--><k/>u</r>");
        javax.xml.stream.events.StartDocument sd = (javax.xml.stream.events.StartDocument) r.nextEvent();
        check(sd.isStartDocument() && sd.getEventType() == 7, "first event StartDocument(7)");
        javax.xml.stream.events.StartElement se = r.nextEvent().asStartElement();
        check("r".equals(se.getName().getLocalPart()) && "".equals(se.getName().getNamespaceURI())
            && "".equals(se.getName().getPrefix()), "root StartElement QName with empty ns/prefix");
        java.util.Iterator<javax.xml.stream.events.Attribute> ats = se.getAttributes();
        int n = 0;
        StringBuilder sb = new StringBuilder();
        while (ats.hasNext()) {
            javax.xml.stream.events.Attribute at = ats.next();
            sb.append(at.getName().getLocalPart()).append('=').append(at.getValue()).append(';');
            n++;
        }
        check(n == 2 && "x=1;y=2;".equals(sb.toString()), "attributes iterate in document order, got " + sb);
        check(!se.getNamespaces().hasNext(), "no namespace decls on root");
        check(se.getNamespaceContext() != null, "StartElement exposes NamespaceContext");
        javax.xml.stream.events.Characters ch = r.nextEvent().asCharacters();
        check(ch.isCharacters() && "t".equals(ch.getData()), "Characters data");
        javax.xml.stream.events.ProcessingInstruction pi =
            (javax.xml.stream.events.ProcessingInstruction) r.nextEvent();
        check(pi.getEventType() == 3 && "pi".equals(pi.getTarget()) && "d".equals(pi.getData()), "PI target/data");
        javax.xml.stream.events.Comment cm = (javax.xml.stream.events.Comment) r.nextEvent();
        check(cm.getEventType() == 5 && "c".equals(cm.getText()), "Comment text");
        javax.xml.stream.events.StartElement k = r.nextEvent().asStartElement();
        check("k".equals(k.getName().getLocalPart()), "child StartElement");
        javax.xml.stream.events.EndElement ke = r.nextEvent().asEndElement();
        check("k".equals(ke.getName().getLocalPart()), "child EndElement");
        check(r.nextEvent().asCharacters().getData().equals("u"), "trailing Characters");
        javax.xml.stream.events.EndElement ee = r.nextEvent().asEndElement();
        check("r".equals(ee.getName().getLocalPart()), "root EndElement");
        javax.xml.stream.events.XMLEvent ed = r.nextEvent();
        check(ed.getEventType() == 8 && ed.isEndDocument(), "final EndDocument(8)");
        check(!r.hasNext(), "exhausted after EndDocument");
        r.close();
    }

    // ---- Deepened 2: peek does not consume; Iterator interop ----
    static void testPeekAndIterate() throws Exception {
        javax.xml.stream.XMLEventReader r = er("<a><b/>x</a>");
        r.nextEvent(); // SD
        javax.xml.stream.events.XMLEvent p1 = r.peek();
        javax.xml.stream.events.XMLEvent p2 = r.peek();
        check(p1.getEventType() == 1 && p2.getEventType() == 1,
            "repeated peek returns START_ELEMENT both times");
        check(p1 == p2, "repeated peek yields same event instance (no consumption)");
        javax.xml.stream.events.XMLEvent nx = r.nextEvent();
        check(nx.getEventType() == 1 && "a".equals(nx.asStartElement().getName().getLocalPart()),
            "nextEvent after peek returns the peeked event");
        // Iterator.next() delegates to nextEvent()
        java.util.Iterator<?> it = r;
        Object o = it.next();
        check(o instanceof javax.xml.stream.events.XMLEvent
            && ((javax.xml.stream.events.XMLEvent) o).isStartElement(), "Iterator.next() returns XMLEvent");
        javax.xml.stream.events.XMLEvent ch = r.peek();
        check(ch.isEndElement() && "b".equals(ch.asEndElement().getName().getLocalPart()), "peek sees END b");
        while (r.hasNext()) r.nextEvent();
        check(r.peek() == null, "peek at exhaustion returns null");
        r.close();
    }

    // ---- Deepened 3: getElementText / nextTag ----
    static void testElementTextAndNextTag() throws Exception {
        javax.xml.stream.XMLEventReader r = er("<r><e1>x&amp;y</e1><e2/></r>");
        r.nextEvent(); // SD
        r.nextEvent(); // r
        r.nextEvent(); // e1
        String t = r.getElementText();
        check("x&y".equals(t), "getElementText decodes entities, got [" + t + "]");
        check(r.peek().getEventType() == 1, "event-side getElementText consumes END_ELEMENT too (peek -> next element)");
        check(r.nextEvent().isStartElement(), "next event after consumed element is START e2");
        r.close();
        // nextTag skips whitespace-only events
        javax.xml.stream.XMLEventReader r2 = er("<a> <!--x--> <b/></a>");
        javax.xml.stream.events.XMLEvent tag = r2.nextTag();
        check(tag.isStartElement() && "a".equals(tag.asStartElement().getName().getLocalPart()), "nextTag to root");
        check(r2.nextTag().isStartElement(), "nextTag skips ws/comment to child");
        r2.close();
        // getElementText on non-START_ELEMENT -> XMLStreamException (impl behavior, javadoc says XMLStreamException)
        javax.xml.stream.XMLEventReader r3 = er("<a>t</a>");
        r3.nextEvent(); r3.nextEvent(); r3.nextEvent(); // SD, SE, CH
        boolean thrown = false;
        try { r3.getElementText(); }
        catch (javax.xml.stream.XMLStreamException e) { thrown = true; }
        check(thrown, "getElementText off START_ELEMENT throws XMLStreamException");
        r3.close();
    }

    // ---- Deepened 4: negative paths ----
    static void testNegativePaths() throws Exception {
        // remove -> UnsupportedOperationException (read-only iterator)
        javax.xml.stream.XMLEventReader r = er("<a/>");
        r.nextEvent(); r.nextEvent(); r.nextEvent();
        boolean uoe = false;
        try { r.remove(); }
        catch (java.lang.UnsupportedOperationException e) { uoe = true; }
        check(uoe, "remove() -> UnsupportedOperationException");
        r.close();
        // nextEvent past end -> NoSuchElementException
        javax.xml.stream.XMLEventReader r2 = er("<a/>");
        while (r2.hasNext()) r2.nextEvent();
        boolean nsee = false;
        try { r2.nextEvent(); }
        catch (java.util.NoSuchElementException e) { nsee = true; }
        check(nsee, "nextEvent() past end -> NoSuchElementException");
        r2.close();
        // getProperty on reader: unknown returns null on JDK impl
        javax.xml.stream.XMLEventReader r3 = er("<a/>");
        check(r3.getProperty("no/such/prop") == null, "event reader getProperty(unknown) returns null");
        // close() then nextEvent(): JDK keeps the underlying reader usable
        javax.xml.stream.XMLEventReader r4 = er("<a><b/></a>");
        r4.nextEvent();
        r4.close();
        boolean afterClose = false;
        try { afterClose = r4.nextEvent().isStartElement(); } catch (Throwable t) { afterClose = false; }
        check(afterClose, "JDK-8: nextEvent() after close() continues (locked empirical)");
        // entity reference through the event stream (replacement disabled)
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        f.setProperty(javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        javax.xml.stream.XMLEventReader r5 = f.createXMLEventReader(new java.io.StringReader(
            "<!DOCTYPE r [<!ENTITY e \"RR\">]><r>&e;</r>"));
        java.util.List<Integer> codes = new java.util.ArrayList<Integer>();
        String entName = null;
        while (r5.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r5.nextEvent();
            codes.add(e.getEventType());
            if (e.isEntityReference()) entName = ((javax.xml.stream.events.EntityReference) e).getName();
        }
        r5.close();
        check(codes.equals(java.util.Arrays.asList(7, 11, 1, 9, 2, 8)), "event codes with entity kept, got " + codes);
        check("e".equals(entName), "EntityReference event name");
        // StartDocument facts from decl
        javax.xml.stream.XMLEventReader r6 = er("<?xml version='1.1' encoding='UTF-8'?><a/>");
        javax.xml.stream.events.StartDocument sd6 = (javax.xml.stream.events.StartDocument) r6.nextEvent();
        check("1.1".equals(sd6.getVersion()), "StartDocument.getVersion from decl");
        check(sd6.getCharacterEncodingScheme() == null,
            "JDK-8 StartDocument.getCharacterEncodingScheme null for Reader input (locked empirical)");
        check(sd6.standaloneSet() == true,
            "JDK-8 quirk: StartDocument.standaloneSet() always true even when standalone absent (locked; JDK-27 returns false)");
        r6.close();
    }
}
