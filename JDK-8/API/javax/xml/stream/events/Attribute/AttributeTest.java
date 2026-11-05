/*
 * @test
 * @summary javax.xml.stream.events.Attribute conformance: interface hierarchy and methods.
 * @run main AttributeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttributeTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.Attribute");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "has method getName");
        check(names.contains("getValue"), "has method getValue");
        check(names.contains("getDTDType"), "has method getDTDType");
        check(names.contains("isSpecified"), "has method isSpecified");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (factory 3 forms + parsed isSpecified matrix) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory (local, value): no-namespace QName, CDATA type, not specified, code 10
        javax.xml.stream.events.Attribute a1 = ef.createAttribute("k", "v");
        check("k".equals(a1.getName().getLocalPart()) && "".equals(a1.getName().getNamespaceURI())
                && "".equals(a1.getName().getPrefix()), "factory attr QName {''}k");
        check("v".equals(a1.getValue()), "factory attr value");
        check("CDATA".equals(a1.getDTDType()), "factory attr DTDType CDATA default");
        check(!a1.isSpecified(), "factory attr isSpecified false (impl)");
        check(a1.getEventType() == 10 && a1.isAttribute() && !a1.isNamespace(), "factory attr code 10 + family");

        // 2) factory (nsURI, local, prefix, value): full QName
        javax.xml.stream.events.Attribute a2 = ef.createAttribute("pb", "http://p", "b", "2");
        check("http://p".equals(a2.getName().getNamespaceURI()) && "b".equals(a2.getName().getLocalPart())
                && "pb".equals(a2.getName().getPrefix()) && "2".equals(a2.getValue()), "factory 4-arg attr (prefix,ns,local,value)");

        // 3) factory (QName, value)
        javax.xml.stream.events.Attribute a3 = ef.createAttribute(new javax.xml.namespace.QName("http://q", "c", "q"), "3");
        check(a3.getName().equals(new javax.xml.namespace.QName("http://q", "c", "q")) && "3".equals(a3.getValue()),
                "factory QName-arg attr");
        // null local name -> IllegalArgumentException
        try { ef.createAttribute((String) null, "v"); check(false, "createAttribute(null,v) must IAE"); }
        catch (IllegalArgumentException iae) { passed++; }

        // 4) parsed attributes: specified=true (differs from factory), CDATA type, doc order
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<r a='1' b='2'/>"));
        r.nextEvent();
        javax.xml.stream.events.StartElement se = r.nextEvent().asStartElement();
        String[] order = {"a", "b"};
        int i = 0;
        for (java.util.Iterator<?> it = se.getAttributes(); it.hasNext(); ) {
            javax.xml.stream.events.Attribute at = (javax.xml.stream.events.Attribute) it.next();
            check(at.getName().getLocalPart().equals(order[i]), "parsed attr order[" + i + "]");
            check(at.isSpecified(), "parsed attr isSpecified true");
            check("CDATA".equals(at.getDTDType()), "parsed attr DTDType CDATA");
            check(at.getEventType() == 10, "parsed attr code 10");
            i++;
        }
        check(i == 2, "parsed attr count 2");
        check("1".equals(se.getAttributeByName(new javax.xml.namespace.QName("a")).getValue()),
                "getAttributeByName wired to same value");
        r.close();

        // 5) parsed namespaced attribute: QName with namespace + prefix
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<r xmlns:p='http://p' p:a='v'/>"));
        r2.nextEvent();
        javax.xml.stream.events.Attribute pa = (javax.xml.stream.events.Attribute)
                r2.nextEvent().asStartElement().getAttributes().next();
        check("http://p".equals(pa.getName().getNamespaceURI()) && "a".equals(pa.getName().getLocalPart())
                && "p".equals(pa.getName().getPrefix()) && "v".equals(pa.getValue()), "parsed namespaced attr QName");
        r2.close();

        // 6) attributes are never surfaced as standalone events by the reader
        javax.xml.stream.XMLEventReader r3 = ifac.createXMLEventReader(
                new java.io.StringReader("<r a='1'/>"));
        boolean sawAttrEvent = false;
        while (r3.hasNext()) {
            if (r3.nextEvent().getEventType() == 10) sawAttrEvent = true;
        }
        check(!sawAttrEvent, "no standalone ATTRIBUTE events from reader");
        r3.close();

        // 7) encoded form: attribute rendered inside start element
        java.io.StringWriter w = new java.io.StringWriter();
        java.util.List<javax.xml.stream.events.Attribute> one =
                new java.util.ArrayList<javax.xml.stream.events.Attribute>();
        one.add(a1);
        ef.createStartElement("", "", "r", one.iterator(),
                java.util.Collections.<javax.xml.stream.events.Namespace>emptyIterator()).writeAsEncodedUnicode(w);
        check(w.toString().contains("k='v'"), "encoded SE contains k='v'");
    }
}
