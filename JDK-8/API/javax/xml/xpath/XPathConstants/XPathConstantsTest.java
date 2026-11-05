/*
 * @test
 * @summary javax.xml.xpath.XPathConstants conformance: xpath constants.
 * @run main XPathConstantsTest
 */


public class XPathConstantsTest {
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
        check(javax.xml.xpath.XPathConstants.NUMBER != null, "NUMBER");
        check(javax.xml.xpath.XPathConstants.STRING != null, "STRING");
        check(javax.xml.xpath.XPathConstants.BOOLEAN != null, "BOOLEAN");
        check(javax.xml.xpath.XPathConstants.NODESET != null, "NODESET");
        check(javax.xml.xpath.XPathConstants.NODE != null, "NODE");
        check("http://java.sun.com/jaxp/xpath/dom".equals(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL), "DOM_OBJECT_MODEL");
        testQNameContract();
        testUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    // The five return-type QNames use the XPath 1.0 (= XSLT) namespace, NOT the
    // XPath language URI; locked actual per javadoc and runtime.
    static void testQNameContract() {
        String NS = "http://www.w3.org/1999/XSL/Transform";
        javax.xml.namespace.QName[] all = {
            javax.xml.xpath.XPathConstants.NUMBER,
            javax.xml.xpath.XPathConstants.STRING,
            javax.xml.xpath.XPathConstants.BOOLEAN,
            javax.xml.xpath.XPathConstants.NODESET,
            javax.xml.xpath.XPathConstants.NODE,
        };
        String[] locals = {"NUMBER", "STRING", "BOOLEAN", "NODESET", "NODE"};
        for (int i = 0; i < all.length; i++) {
            check(NS.equals(all[i].getNamespaceURI()), locals[i] + " namespace is XSL/XPath 1.0 ns");
            check(locals[i].equals(all[i].getLocalPart()), locals[i] + " localPart exact");
        }
        // constants are static final: identical instances on repeated reads
        check(javax.xml.xpath.XPathConstants.NUMBER == javax.xml.xpath.XPathConstants.NUMBER,
              "constants are identity-stable");
        // all five distinct from one another
        java.util.Set<javax.xml.namespace.QName> set = new java.util.HashSet<javax.xml.namespace.QName>();
        for (javax.xml.namespace.QName q : all) set.add(q);
        check(set.size() == 5, "five distinct QNames");
        // prefix not part of these constants
        check(javax.xml.xpath.XPathConstants.NUMBER.getPrefix() == null
              || javax.xml.xpath.XPathConstants.NUMBER.getPrefix().isEmpty(),
              "NUMBER prefix null/empty");
        // equality by (ns, local) per QName contract
        check(javax.xml.xpath.XPathConstants.STRING
                  .equals(new javax.xml.namespace.QName(NS, "STRING")),
              "STRING equals same (ns, local) QName built by caller");
    }

    // ---- each constant drives the matching evaluate return type (closed loop) ----
    static void testUsage() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document d = dbf.newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root><a>1</a><a>2</a></root>".getBytes("UTF-8")));
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();

        check(xp.evaluate("count(/root/a)", d, javax.xml.xpath.XPathConstants.NUMBER) instanceof Double,
              "NUMBER drives Double result");
        check(xp.evaluate("/root/a[1]", d, javax.xml.xpath.XPathConstants.STRING) instanceof String,
              "STRING drives String result");
        check(xp.evaluate("count(/root/a) = 2", d, javax.xml.xpath.XPathConstants.BOOLEAN) instanceof Boolean,
              "BOOLEAN drives Boolean result");
        check(xp.evaluate("/root/a", d, javax.xml.xpath.XPathConstants.NODESET) instanceof org.w3c.dom.NodeList,
              "NODESET drives NodeList result");
        check(xp.evaluate("/root/a[1]", d, javax.xml.xpath.XPathConstants.NODE) instanceof org.w3c.dom.Node,
              "NODE drives Node result");

        // DOM_OBJECT_MODEL string is accepted by the factory
        check(javax.xml.xpath.XPathFactory.newInstance(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL) != null,
              "DOM_OBJECT_MODEL accepted by XPathFactory.newInstance");
    }
}
