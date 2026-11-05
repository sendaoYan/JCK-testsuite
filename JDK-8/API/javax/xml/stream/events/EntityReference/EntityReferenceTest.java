/*
 * @test
 * @summary javax.xml.stream.events.EntityReference conformance: interface hierarchy and methods.
 * @run main EntityReferenceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class EntityReferenceTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.EntityReference");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDeclaration"), "has method getDeclaration");
        check(names.contains("getName"), "has method getName");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (factory + parsed with replacing on/off) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory createEntityReference: name kept, null declaration tolerated, code 9
        javax.xml.stream.events.EntityReference er = ef.createEntityReference("foo", null);
        check("foo".equals(er.getName()), "factory ER name");
        check(er.getDeclaration() == null, "factory ER declaration null");
        check(er.getEventType() == 9 && er.isEntityReference(), "factory ER code 9 + family");
        check(!er.isCharacters() && !er.isStartElement() && !er.isEndElement(), "factory ER isXxx rest false");
        java.io.StringWriter w = new java.io.StringWriter();
        er.writeAsEncodedUnicode(w);
        check("&foo;".equals(w.toString()), "factory ER encoded &foo;");
        javax.xml.stream.events.EntityReference ern = ef.createEntityReference(null, null);
        check(ern.getName() == null, "createEntityReference(null,null) tolerated");

        // 2) parsed with IS_REPLACING_ENTITY_REFERENCES=false: internal entity yields ER event
        javax.xml.stream.XMLInputFactory nf = javax.xml.stream.XMLInputFactory.newFactory();
        nf.setProperty(javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        javax.xml.stream.XMLEventReader r = nf.createXMLEventReader(
                new java.io.StringReader("<!DOCTYPE r [<!ENTITY e1 'Hello'>]><r>&e1;</r>"));
        boolean saw = false;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e.isEntityReference()) {
                saw = true;
                javax.xml.stream.events.EntityReference per = (javax.xml.stream.events.EntityReference) e;
                check("e1".equals(per.getName()), "parsed ER name e1");
                javax.xml.stream.events.EntityDeclaration d = per.getDeclaration();
                check(d != null && "e1".equals(d.getName()) && "Hello".equals(d.getReplacementText()),
                        "parsed ER declaration wired to internal entity");
                check(e.getEventType() == 9, "parsed ER code 9");
            }
        }
        check(saw, "ENTITY_REFERENCE event observed with replacing=false");
        r.close();

        // 3) with replacing=true (default): no ER event, replacement characters instead
        javax.xml.stream.XMLEventReader r2 = javax.xml.stream.XMLInputFactory.newFactory()
                .createXMLEventReader(new java.io.StringReader("<!DOCTYPE r [<!ENTITY e1 'Hello'>]><r>&e1;</r>"));
        boolean saw2 = false; String text = "";
        while (r2.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r2.nextEvent();
            if (e.isEntityReference()) saw2 = true;
            if (e.isCharacters()) text = e.asCharacters().getData();
        }
        check(!saw2 && "Hello".equals(text), "replacing=true replaces entity with characters");
        r2.close();

        // 4) unparsed entity (NDATA) in content -> XMLStreamException
        try {
            javax.xml.stream.XMLEventReader r3 = javax.xml.stream.XMLInputFactory.newFactory()
                    .createXMLEventReader(new java.io.StringReader(
                            "<!DOCTYPE r [<!ENTITY e2 SYSTEM 'x.bin' NDATA gif><!NOTATION gif SYSTEM 'fmt'>]><r>&e2;</r>"));
            while (r3.hasNext()) r3.nextEvent();
            check(false, "unparsed entity reference must throw");
        } catch (javax.xml.stream.XMLStreamException xse) {
            check(String.valueOf(xse.getMessage()).contains("not permitted"), "unparsed entity message");
        }
    }
}
