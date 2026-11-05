/*
 * @test
 * @summary javax.xml.stream.events.DTD conformance: interface hierarchy and methods.
 * @run main DTDTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DTDTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.DTD");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDocumentTypeDeclaration"), "has method getDocumentTypeDeclaration");
        check(names.contains("getProcessedDTD"), "has method getProcessedDTD");
        check(names.contains("getNotations"), "has method getNotations");
        check(names.contains("getEntities"), "has method getEntities");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (full/empty/absent DTD matrix + factory) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) full internal subset: exact declaration string, entities and notations lists
        String decl = "<!DOCTYPE root [\n"
                + "<!ENTITY e1 'Hello'>\n"
                + "<!ENTITY e2 SYSTEM 'ent.bin' NDATA bin>\n"
                + "<!NOTATION bin SYSTEM 'binfmt'>\n"
                + "<!NOTATION gif PUBLIC '-//gif//gif'>\n"
                + "]>";
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader(decl + "\n<root/>"));
        javax.xml.stream.events.DTD dtd = null;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) dtd = (javax.xml.stream.events.DTD) e;
        }
        r.close();
        check(dtd != null, "DTD event present");
        check(dtd.getEventType() == 11, "DTD code 11");
        check(decl.equals(dtd.getDocumentTypeDeclaration()), "DTD declaration string exact");
        check(dtd.getEntities() != null && dtd.getEntities().size() == 2, "two entities");
        check("e1".equals(((javax.xml.stream.events.EntityDeclaration) dtd.getEntities().get(0)).getName())
                && "e2".equals(((javax.xml.stream.events.EntityDeclaration) dtd.getEntities().get(1)).getName()),
                "entity names in order");
        check(dtd.getNotations() != null && dtd.getNotations().size() == 2, "two notations");
        check("bin".equals(((javax.xml.stream.events.NotationDeclaration) dtd.getNotations().get(0)).getName())
                && "gif".equals(((javax.xml.stream.events.NotationDeclaration) dtd.getNotations().get(1)).getName()),
                "notation names in order");
        check(dtd.getProcessedDTD() == null, "processed DTD null");
        check(!dtd.isCharacters() && !dtd.isStartElement(), "DTD isXxx family");

        // 2) empty internal subset: null entity/notation lists
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<!DOCTYPE r ><r/>"));
        javax.xml.stream.events.DTD dtd2 = null;
        while (r2.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r2.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) dtd2 = (javax.xml.stream.events.DTD) e;
        }
        r2.close();
        check(dtd2 != null, "empty-subset DTD event present");
        check("<!DOCTYPE r >".equals(dtd2.getDocumentTypeDeclaration()), "empty subset declaration exact");
        check(dtd2.getEntities() == null && dtd2.getNotations() == null, "empty subset null lists");

        // 3) no DOCTYPE at all: no DTD event
        javax.xml.stream.XMLEventReader r3 = ifac.createXMLEventReader(new java.io.StringReader("<r/>"));
        boolean saw = false;
        while (r3.hasNext()) { if (r3.nextEvent().getEventType() == 11) saw = true; }
        r3.close();
        check(!saw, "no DTD event without DOCTYPE");

        // 4) factory createDTD: string round-trip, null lists, code, encoded
        javax.xml.stream.events.DTD fd = ef.createDTD(decl);
        check(decl.equals(fd.getDocumentTypeDeclaration()), "factory DTD string round-trip");
        check(fd.getEntities() == null && fd.getNotations() == null && fd.getProcessedDTD() == null,
                "factory DTD null lists (declarations not parsed)");
        check(fd.getEventType() == 11, "factory DTD code 11");
        java.io.StringWriter w = new java.io.StringWriter();
        fd.writeAsEncodedUnicode(w);
        check(decl.equals(w.toString()), "factory DTD encoded exact");
        java.io.StringWriter w2 = new java.io.StringWriter();
        ef.createDTD("<!DOCTYPE r>").writeAsEncodedUnicode(w2);
        check("<!DOCTYPE r>".equals(w2.toString()), "factory DTD minimal encoded");
        try { fd.asCharacters(); check(false, "DTD.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }
    }
}
