/*
 * @test
 * @summary javax.xml.namespace.NamespaceContext conformance: interface contract via a custom implementation (prefix/uri/prefixes).
 * @run main NamespaceContextTest
 */

import javax.xml.namespace.NamespaceContext;

import java.util.Arrays;
import java.util.Iterator;

public class NamespaceContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // deterministic fixed mapping: prefix "p" <-> namespace "urn:p"
    static class FixedContext implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            if (prefix == null) throw new IllegalArgumentException("prefix null");
            if ("p".equals(prefix)) return "urn:p";
            if ("xml".equals(prefix)) return javax.xml.XMLConstants.XML_NS_URI;
            return javax.xml.XMLConstants.NULL_NS_URI;
        }
        public String getPrefix(String uri) {
            if (uri == null) throw new IllegalArgumentException("uri null");
            if ("urn:p".equals(uri)) return "p";
            if (javax.xml.XMLConstants.XML_NS_URI.equals(uri)) return "xml";
            return null;
        }
        public Iterator getPrefixes(String uri) {
            if (uri == null) throw new IllegalArgumentException("uri null");
            if ("urn:p".equals(uri)) return Arrays.asList("p").iterator();
            return Arrays.asList(new String[0]).iterator();
        }
    }

    public static void main(String[] args) throws Exception {
        NamespaceContext nc = new FixedContext();

        check("urn:p".equals(nc.getNamespaceURI("p")), "getNamespaceURI resolves prefix");
        check(javax.xml.XMLConstants.NULL_NS_URI.equals(nc.getNamespaceURI("unknown")), "unknown prefix -> NULL_NS_URI");
        check(javax.xml.XMLConstants.XML_NS_URI.equals(nc.getNamespaceURI("xml")), "xml prefix -> XML_NS_URI");
        check("p".equals(nc.getPrefix("urn:p")), "getPrefix resolves namespace");
        check(nc.getPrefix("urn:unknown") == null, "getPrefix unknown -> null");

        Iterator prefixes = nc.getPrefixes("urn:p");
        check(prefixes.hasNext() && "p".equals(prefixes.next()), "getPrefixes returns matching prefix");
        Iterator none = nc.getPrefixes("urn:unknown");
        check(!none.hasNext(), "getPrefixes unknown -> empty");

        // null args -> IllegalArgumentException
        boolean npe1 = false;
        try { nc.getNamespaceURI(null); } catch (IllegalArgumentException e) { npe1 = true; }
        check(npe1, "getNamespaceURI(null) -> IAE");
        boolean npe2 = false;
        try { nc.getPrefix(null); } catch (IllegalArgumentException e) { npe2 = true; }
        check(npe2, "getPrefix(null) -> IAE");

        testMultiPrefixContext();
        testJdkInternalContext();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: multi-prefix mapping contract =================
    static class MultiPrefixContext implements NamespaceContext {
        // deterministic fixed mapping: u1 bound to two prefixes, default ns, built-ins
        public String getNamespaceURI(String prefix) {
            if (prefix == null) throw new IllegalArgumentException("prefix null");
            if ("p1".equals(prefix) || "p2".equals(prefix)) return "urn:u1";
            if ("d".equals(prefix)) return "urn:default";
            if ("xml".equals(prefix)) return javax.xml.XMLConstants.XML_NS_URI;
            if ("xmlns".equals(prefix)) return javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            return javax.xml.XMLConstants.NULL_NS_URI;
        }
        public String getPrefix(String uri) {
            if (uri == null) throw new IllegalArgumentException("uri null");
            if ("urn:u1".equals(uri)) return "p1"; // first binding wins
            if ("urn:default".equals(uri)) return "d";
            if (javax.xml.XMLConstants.XML_NS_URI.equals(uri)) return "xml";
            return null;
        }
        public Iterator getPrefixes(String uri) {
            if (uri == null) throw new IllegalArgumentException("uri null");
            if ("urn:u1".equals(uri)) return Arrays.asList("p1", "p2").iterator();
            if ("urn:default".equals(uri)) return Arrays.asList("d").iterator();
            if (javax.xml.XMLConstants.XML_NS_URI.equals(uri)) return Arrays.asList("xml").iterator();
            return java.util.Collections.<String>emptyList().iterator();
        }
    }

    static void testMultiPrefixContext() {
        NamespaceContext nc = new MultiPrefixContext();
        // both prefixes resolve to the same namespace
        check("urn:u1".equals(nc.getNamespaceURI("p1")) && "urn:u1".equals(nc.getNamespaceURI("p2")),
              "two prefixes share one namespace");
        // single-valued getPrefix returns one of the bound prefixes
        check("p1".equals(nc.getPrefix("urn:u1")), "getPrefix returns the deterministic first binding");
        // getPrefixes lists BOTH bound prefixes
        java.util.Set<String> seen = new java.util.TreeSet<String>();
        Iterator it = nc.getPrefixes("urn:u1");
        while (it.hasNext()) seen.add((String) it.next());
        check(seen.size() == 2 && seen.contains("p1") && seen.contains("p2"),
              "getPrefixes yields both prefixes (order-independent)");
        // iterators are independent per call
        Iterator it2 = nc.getPrefixes("urn:u1");
        check(it2.hasNext() && "p1".equals(it2.next()) && it2.hasNext() && "p2".equals(it2.next()) && !it2.hasNext(),
              "fresh iterator traverses independently");
        // every returned prefix maps back to the same URI (round invariant)
        boolean invariant = true;
        for (String p : seen) if (!"urn:u1".equals(nc.getNamespaceURI(p))) invariant = false;
        check(invariant, "prefix->URI invariant holds for all returned prefixes");
        // unknown prefix -> NULL_NS_URI per javadoc; unknown URI -> null / empty iterator
        check(javax.xml.XMLConstants.NULL_NS_URI.equals(nc.getNamespaceURI("zz")),
              "unbound prefix resolves to NULL_NS_URI");
        check(nc.getPrefix("urn:zz") == null, "unbound URI -> null prefix");
        check(!nc.getPrefixes("urn:zz").hasNext(), "unbound URI -> empty iterator");
        // built-in xml/xmlns bindings
        check(javax.xml.XMLConstants.XML_NS_URI.equals(nc.getNamespaceURI("xml")), "xml built-in");
        check(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(nc.getNamespaceURI("xmlns")), "xmlns built-in");
        check("xml".equals(nc.getPrefix(javax.xml.XMLConstants.XML_NS_URI)), "XML ns URI -> xml prefix");
    }

    // ================= deepening: JDK-internal NamespaceContext (StAX reader) =================
    static void testJdkInternalContext() throws Exception {
        // fixed document with one bound prefix and a default namespace
        javax.xml.stream.XMLInputFactory xif = javax.xml.stream.XMLInputFactory.newInstance();
        java.io.StringReader sr = new java.io.StringReader("<r xmlns:p=\"urn:p\" xmlns=\"urn:default\">x</r>");
        javax.xml.stream.XMLStreamReader r = xif.createXMLStreamReader(sr);
        r.next(); // positioned on <r>
        NamespaceContext nc = r.getNamespaceContext();

        check("urn:p".equals(nc.getNamespaceURI("p")), "JDK ctx: bound prefix resolves");
        check(javax.xml.XMLConstants.XML_NS_URI.equals(nc.getNamespaceURI("xml")),
              "JDK ctx: xml prefix is built-in");
        check("urn:default".equals(nc.getNamespaceURI("")),
              "JDK ctx: empty prefix resolves to default ns");
        check(nc.getNamespaceURI("zz") == null, "JDK ctx: unbound prefix -> null (impl reports null, javadoc suggests NULL_NS_URI)");
        check("p".equals(nc.getPrefix("urn:p")), "JDK ctx: URI -> prefix");
        check("".equals(nc.getPrefix("urn:default")), "JDK ctx: default ns -> empty prefix");
        check(nc.getPrefix("urn:none") == null, "JDK ctx: unbound URI -> null");

        java.util.Set<String> seen = new java.util.TreeSet<String>();
        Iterator it = nc.getPrefixes("urn:p");
        while (it.hasNext()) seen.add((String) it.next());
        check(seen.size() == 1 && seen.contains("p"), "JDK ctx: getPrefixes(urn:p) == {p} exactly");
        // default namespace URI maps back to the empty prefix
        java.util.Set<String> defSeen = new java.util.TreeSet<String>();
        Iterator defIt = nc.getPrefixes("urn:default");
        while (defIt.hasNext()) defSeen.add((String) defIt.next());
        check(defSeen.size() == 1 && defSeen.contains(""), "JDK ctx: getPrefixes(urn:default) == {\"\"} exactly");

        // null arguments -> IllegalArgumentException in the JDK implementation (matches javadoc)
        boolean iae1 = false, iae2 = false, iae3 = false;
        try { nc.getNamespaceURI(null); } catch (IllegalArgumentException e) { iae1 = true; }
        try { nc.getPrefix(null); } catch (IllegalArgumentException e) { iae2 = true; }
        try { nc.getPrefixes(null); } catch (IllegalArgumentException e) { iae3 = true; }
        check(iae1 && iae2 && iae3, "JDK ctx: null args -> IllegalArgumentException for all three methods");
        r.close();
    }
}
