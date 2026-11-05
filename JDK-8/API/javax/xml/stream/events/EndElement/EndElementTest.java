/*
 * @test
 * @summary javax.xml.stream.events.EndElement conformance: interface hierarchy and methods.
 * @run main EndElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class EndElementTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.EndElement");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "has method getName");
        check(names.contains("getNamespaces"), "has method getNamespaces");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (parsed + factory EndElement) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) parsed EndElement: code 2, name exact, isXxx family, self cast, empty namespaces
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<p:r xmlns='http://d' xmlns:p='http://p'>x</p:r>"));
        r.nextEvent();
        r.nextEvent();
        r.nextEvent();
        javax.xml.stream.events.XMLEvent e = r.nextEvent();
        check(e.getEventType() == 2 && e.isEndElement() && !e.isStartElement() && !e.isCharacters(),
                "EE code 2 + family");
        javax.xml.stream.events.EndElement ee = e.asEndElement();
        check(ee == e, "asEndElement self");
        check("http://p".equals(ee.getName().getNamespaceURI()) && "r".equals(ee.getName().getLocalPart())
                && "p".equals(ee.getName().getPrefix()), "EE QName (http://p, r, p)");
        // JDK impl: parsed EndElement reports no out-of-scope namespaces even when declared at this element
        check(!ee.getNamespaces().hasNext(), "parsed EE namespaces empty (impl)");
        r.close();

        // 2) factory createEndElement(QName, nsIterator): name kept, namespace iterator swallowed by impl
        java.util.List<javax.xml.stream.events.Namespace> nl = new java.util.ArrayList<javax.xml.stream.events.Namespace>();
        nl.add(ef.createNamespace("p", "http://p"));
        javax.xml.stream.events.EndElement f1 =
                ef.createEndElement(new javax.xml.namespace.QName("http://d", "root", "p"), nl.iterator());
        check(f1.getEventType() == 2 && f1.isEndElement(), "factory EE code 2");
        check("http://d".equals(f1.getName().getNamespaceURI()) && "root".equals(f1.getName().getLocalPart())
                && "p".equals(f1.getName().getPrefix()), "factory EE QName");
        check(!f1.getNamespaces().hasNext(), "factory EE namespace iterator swallowed (impl)");

        // 3) factory (prefix, nsURI, local) and (+nsIterator) forms
        javax.xml.stream.events.EndElement f2 = ef.createEndElement("p", "http://p", "local");
        check(f2.getName().getPrefix().equals("p") && f2.getName().getLocalPart().equals("local")
                && f2.getName().getNamespaceURI().equals("http://p"), "factory EE 3-arg name");
        javax.xml.stream.events.EndElement f3 = ef.createEndElement("p", "http://p", "local", nl.iterator());
        check(f3.getName().equals(f2.getName()) && !f3.getNamespaces().hasNext(), "factory EE 4-arg name + empty ns");

        // 4) writeAsEncodedUnicode exact (prefix:local form, unlike the bracket start form)
        java.io.StringWriter w = new java.io.StringWriter();
        f2.writeAsEncodedUnicode(w);
        check("</p:local>".equals(w.toString()), "factory EE encoded </p:local>");

        // 5) EndElement as part of the reader flow: after inner elements, order preserved
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<r><c/></r>"));
        r2.nextEvent(); r2.nextEvent(); r2.nextEvent();
        check(r2.nextEvent().asEndElement().getName().getLocalPart().equals("c"), "inner EE name c");
        check(r2.nextEvent().asEndElement().getName().getLocalPart().equals("r"), "outer EE name r");
        check(r2.nextEvent().getEventType() == 8, "END_DOCUMENT after EE");
        r2.close();

        // 6) wrong casts
        try { f2.asCharacters(); check(false, "EE.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }
        try { f2.asStartElement(); check(false, "EE.asStartElement must CCE"); }
        catch (ClassCastException cce) { passed++; }
        check(f2.getSchemaType() == null, "EE getSchemaType null");
    }
}
