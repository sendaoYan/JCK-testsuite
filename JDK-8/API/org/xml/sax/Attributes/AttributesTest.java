/*
 * @test
 * @summary org.xml.sax.Attributes conformance: interface hierarchy and methods.
 * @run main AttributesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttributesTest {
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
        Class<?> c = Class.forName("org.xml.sax.Attributes");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLength"), "has method getLength");
        check(names.contains("getURI"), "has method getURI");
        check(names.contains("getLocalName"), "has method getLocalName");
        check(names.contains("getQName"), "has method getQName");
        check(names.contains("getType"), "has method getType");
        check(names.contains("getValue"), "has method getValue");
        check(names.contains("getIndex"), "has method getIndex");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Attributes contract (live callback object) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setFeature("http://xml.org/sax/features/namespaces", true);
        xr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        final org.xml.sax.Attributes[] seen = new org.xml.sax.Attributes[1];
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String uri, String ln, String q, org.xml.sax.Attributes a) { seen[0] = a; }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r a='1' p:b='2' xmlns:p='urn:p'/>")));
        org.xml.sax.Attributes a = seen[0];
        check(a.getLength() == 2, "with namespaces on + namespace-prefixes off, xmlns declarations are hidden from Attributes (len=" + a.getLength() + ")");
        check("a".equals(a.getQName(0)) && "a".equals(a.getLocalName(0)) && "".equals(a.getURI(0)), "index accessors: qName/localName/uri");
        check("1".equals(a.getValue(0)) && "1".equals(a.getValue("a")) && "1".equals(a.getValue("", "a")), "getValue by index/qName/uri+local");
        check("2".equals(a.getValue("urn:p", "b")), "namespaced lookup via (uri, local) pair");
        check("CDATA".equals(a.getType(0)) && "CDATA".equals(a.getType("a")) && "CDATA".equals(a.getType("urn:p", "b")), "getType reports CDATA for unparsed defaults");
        check(a.getIndex("a") == 0 && a.getIndex("urn:p", "b") == 1, "getIndex by qName and by (uri,local) pair");
        check(a.getValue("missing") == null && a.getType("missing") == null, "missing qName -> null value and type");
        check(a.getIndex("missing") == -1 && a.getIndex("urn:x", "b") == -1, "missing lookups -> -1");
        check(a.getValue(9) == null && a.getType(9) == null, "out-of-range index -> null (no throw)");
        check("".equals(a.getURI(9)) && a.getLocalName(9) == null && a.getQName(9) == null, "out-of-range: local/qName null; live list getURI returns empty string");
    }
}
