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

    public static void main(String[] args) {
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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
