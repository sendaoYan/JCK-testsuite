/*
 * @test
 * @summary org.xml.sax.helpers.NamespaceSupport conformance: namespace support.
 * @run main NamespaceSupportTest
 */


public class NamespaceSupportTest {
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
        org.xml.sax.helpers.NamespaceSupport ns = new org.xml.sax.helpers.NamespaceSupport();
        check(ns.declarePrefix("p", "urn:test"), "declare prefix");
        check("urn:test".equals(ns.getURI("p")), "get uri");
        check("p".equals(ns.getPrefix("urn:test")), "get prefix");
        ns.pushContext();
        check(ns.declarePrefix("q", "urn:q"), "push declare");
        ns.popContext();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral NamespaceSupport contract (context stack + processName matrix) ----
    static void deepContract() throws Exception {
        check("http://www.w3.org/XML/1998/namespace".equals(org.xml.sax.helpers.NamespaceSupport.XMLNS), "XMLNS constant is the xml-prefix namespace URI");
        org.xml.sax.helpers.NamespaceSupport ns = new org.xml.sax.helpers.NamespaceSupport();
        ns.pushContext();
        check("http://www.w3.org/XML/1998/namespace".equals(ns.getURI("xml")), "xml prefix pre-bound without any declaration");
        check(!ns.declarePrefix("xml", "http://www.w3.org/XML/1998/namespace"), "declarePrefix('xml', official URI) refused");
        check(!ns.declarePrefix("xml", "urn:wrong"), "declarePrefix('xml', wrong URI) refused");
        check(ns.declarePrefix("p", "urn:p"), "declarePrefix('p') accepted");
        String[] r = ns.processName("p:x", new String[3], false);
        check("urn:p".equals(r[0]) && "x".equals(r[1]) && "p:x".equals(r[2]), "processName returns [uri, local, raw]");
        check(ns.processName("q:x", new String[3], false) == null, "undeclared prefix -> null (not an exception)");
        check(ns.processName("xmlns:e", new String[3], false) == null, "xmlns-prefixed ELEMENT name -> null");
        // default namespace: elements adopt it, attributes never do
        check(ns.declarePrefix("", "urn:d"), "declaring the empty prefix is allowed");
        check("urn:d".equals(ns.getURI("")), "getURI('') returns the default namespace");
        String[] elem = ns.processName("bare", new String[3], false);
        String[] attr = ns.processName("bare", new String[3], true);
        check("urn:d".equals(elem[0]) && "bare".equals(elem[1]), "unprefixed element name adopts the default namespace");
        check("".equals(attr[0]), "unprefixed attribute name never adopts the default namespace");
        ns.declarePrefix("", "");
        check(ns.getURI("") == null, "declarePrefix('','') undeclares the default namespace");
        check("".equals(ns.processName("b2", new String[3], false)[0]), "element back in no-namespace after undeclare");
        ns.declarePrefix("", "urn:d");
        // prefix enumerations
        java.util.List<String> all = new java.util.ArrayList<String>();
        for (java.util.Enumeration<?> e = ns.getPrefixes(); e.hasMoreElements(); ) all.add((String) e.nextElement());
        check(all.contains("p") && all.contains("xml"), "getPrefixes includes declared + built-in xml");
        java.util.List<String> decl = new java.util.ArrayList<String>();
        for (java.util.Enumeration<?> e = ns.getDeclaredPrefixes(); e.hasMoreElements(); ) decl.add((String) e.nextElement());
        check(decl.contains("p") && decl.contains("") && !decl.contains("xml"), "getDeclaredPrefixes is current-context only (no xml)");
        check("p".equals(ns.getPrefix("urn:p")), "getPrefix reverse lookup");
        // context stack
        ns.pushContext();
        check("urn:p".equals(ns.getURI("p")), "child context inherits parent declarations");
        ns.declarePrefix("p", "urn:p2");
        check("urn:p2".equals(ns.getURI("p")), "child may rebind an inherited prefix");
        ns.popContext();
        check("urn:p".equals(ns.getURI("p")), "popContext restores the outer binding");
        // reset
        ns.reset();
        check("http://www.w3.org/XML/1998/namespace".equals(ns.getURI("xml")), "reset keeps the built-in xml binding");
        java.util.List<String> afterReset = new java.util.ArrayList<String>();
        for (java.util.Enumeration<?> e = ns.getPrefixes(); e.hasMoreElements(); ) afterReset.add((String) e.nextElement());
        check(afterReset.contains("xml"), "reset leaves xml enumerable");
        check(ns.getURI("p") == null, "reset drops user declarations");
    }
}
