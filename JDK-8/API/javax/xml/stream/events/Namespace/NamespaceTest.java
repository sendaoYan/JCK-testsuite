/*
 * @test
 * @summary javax.xml.stream.events.Namespace conformance: interface hierarchy and methods.
 * @run main NamespaceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class NamespaceTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.Namespace");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.Attribute"), "implements javax.xml.stream.events.Attribute");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPrefix"), "has method getPrefix");
        check(names.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(names.contains("isDefaultNamespaceDeclaration"), "has method isDefaultNamespaceDeclaration");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (default vs prefixed matrix, factory + parsed) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory default-namespace declaration: createNamespace(uri)
        javax.xml.stream.events.Namespace def = ef.createNamespace("http://d");
        check("".equals(def.getPrefix()), "default ns prefix empty");
        check("http://d".equals(def.getNamespaceURI()), "default ns URI");
        check(def.isDefaultNamespaceDeclaration(), "default ns isDefault true");
        check(def.getName().getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")
                && def.getName().getLocalPart().equals(""), "default ns QName {xmlns-ns}''");
        check("http://d".equals(def.getValue()), "default ns getValue = URI");
        check("CDATA".equals(def.getDTDType()) && !def.isSpecified(), "default ns DTDType CDATA, not specified");
        check(def.isNamespace() && !def.isAttribute(), "isNamespace true, isAttribute false");
        check(def.getEventType() == 13, "namespace event code 13");

        // 2) factory prefixed declaration: createNamespace(prefix, uri)
        javax.xml.stream.events.Namespace pref = ef.createNamespace("p", "http://p");
        check("p".equals(pref.getPrefix()) && "http://p".equals(pref.getNamespaceURI()), "prefixed prefix/URI");
        check(!pref.isDefaultNamespaceDeclaration(), "prefixed isDefault false");
        check(pref.getName().getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")
                && pref.getName().getLocalPart().equals("p"), "prefixed ns QName {xmlns-ns}p");
        check("http://p".equals(pref.getValue()), "prefixed getValue = URI");
        // empty prefix explicitly -> default declaration
        javax.xml.stream.events.Namespace emp = ef.createNamespace("", "http://x");
        check("".equals(emp.getPrefix()) && emp.isDefaultNamespaceDeclaration(), "empty prefix -> default decl");
        // single-arg null tolerated, empty prefix
        javax.xml.stream.events.Namespace nul = ef.createNamespace((String) null);
        check("".equals(nul.getPrefix()) && nul.isDefaultNamespaceDeclaration(), "createNamespace(null) default decl");
        // two-arg null prefix -> IllegalArgumentException
        try { ef.createNamespace(null, "http://x"); check(false, "createNamespace(null,uri) must IAE"); }
        catch (IllegalArgumentException iae) { passed++; }

        // 3) parsed namespace declarations from a namespaced element: both entries exact
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<r xmlns='http://d' xmlns:p='http://p'/>"));
        r.nextEvent();
        javax.xml.stream.events.StartElement se = r.nextEvent().asStartElement();
        int n = 0;
        for (java.util.Iterator<?> it = se.getNamespaces(); it.hasNext(); ) {
            javax.xml.stream.events.Namespace ns = (javax.xml.stream.events.Namespace) it.next();
            if (ns.isDefaultNamespaceDeclaration()) {
                check("".equals(ns.getPrefix()) && "http://d".equals(ns.getNamespaceURI())
                        && "".equals(ns.getName().getLocalPart()), "parsed default ns entry exact");
            } else {
                check("p".equals(ns.getPrefix()) && "http://p".equals(ns.getNamespaceURI())
                        && "p".equals(ns.getName().getLocalPart()), "parsed prefixed ns entry exact");
            }
            check(ns.getEventType() == 13 && !ns.isAttribute(), "parsed ns code 13, not attribute");
            check("CDATA".equals(ns.getDTDType()) && !ns.isSpecified(), "parsed ns CDATA/unspecified");
            n++;
        }
        check(n == 2, "two parsed ns declarations");
        check("http://p".equals(se.getNamespaceURI("p")), "parent StartElement lookup by prefix");
        r.close();

        // 4) encoded form of namespace (as attribute-like xmlns writing)
        java.io.StringWriter w = new java.io.StringWriter();
        javax.xml.stream.events.StartElement sef = ef.createStartElement("", "", "r",
                java.util.Collections.<javax.xml.stream.events.Attribute>emptyIterator(),
                java.util.Collections.singletonList(pref).iterator());
        sef.writeAsEncodedUnicode(w);
        check(w.toString().contains("xmlns:p='http://p'"), "encoded SE contains xmlns:p declaration");
    }
}
