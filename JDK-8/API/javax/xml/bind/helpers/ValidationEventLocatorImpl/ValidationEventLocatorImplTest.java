/*
 * @test
 * @summary javax.xml.bind.helpers.ValidationEventLocatorImpl conformance: default state, setter matrix, SAX/DOM/Object constructors and toString.
 * @run main ValidationEventLocatorImplTest
 */


public class ValidationEventLocatorImplTest {
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
        javax.xml.bind.helpers.ValidationEventLocatorImpl l = new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        l.setLineNumber(10);
        check(l.getLineNumber() == 10, "line number");
        l.setColumnNumber(20);
        check(l.getColumnNumber() == 20, "column number");
        l.setOffset(30);
        check(l.getOffset() == 30, "offset");
        l.setURL(new java.net.URL("http://example.com/x"));
        check(l.getURL() != null, "url");
        testDefaults();
        testSetterMatrix();
        testLocatorCtor();
        testSaxCtor();
        testNodeAndObjectCtors();
        testNegative();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // defaults are -1 across the board, not 0 (locked)
    static void testDefaults() {
        javax.xml.bind.helpers.ValidationEventLocatorImpl d =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        check(d.getURL() == null, "default url null");
        check(d.getOffset() == -1, "default offset -1");
        check(d.getLineNumber() == -1, "default line -1");
        check(d.getColumnNumber() == -1, "default column -1");
        check(d.getObject() == null, "default object null");
        check(d.getNode() == null, "default node null");
        check(d instanceof javax.xml.bind.ValidationEventLocator, "implements ValidationEventLocator");
        check(javax.xml.bind.helpers.ValidationEventLocatorImpl.class.getConstructors().length == 5, "5 public constructors");
    }

    static void testSetterMatrix() throws Exception {
        javax.xml.bind.helpers.ValidationEventLocatorImpl l =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        java.net.URL u = new java.net.URL("http://example.com/doc");
        l.setURL(u);
        check(l.getURL() == u, "setURL identity");
        l.setOffset(7);
        check(l.getOffset() == 7, "setOffset readback");
        l.setLineNumber(3);
        check(l.getLineNumber() == 3, "setLineNumber readback");
        l.setColumnNumber(13);
        check(l.getColumnNumber() == 13, "setColumnNumber readback");
        Object o = new Object();
        l.setObject(o);
        check(l.getObject() == o, "setObject identity");
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        org.w3c.dom.Node n = doc.createElement("el");
        l.setNode(n);
        check(l.getNode() == n, "setNode identity");
        // overwrite semantics
        java.net.URL u2 = new java.net.URL("http://example.com/two");
        l.setURL(u2);
        check(l.getURL() == u2, "setURL replaceable");
        l.setURL(null);
        check(l.getURL() == null, "setURL(null) allowed");
        l.setObject(null);
        check(l.getObject() == null, "setObject(null) allowed");
    }

    // Locator constructor maps systemId->URL plus line/column
    static void testLocatorCtor() {
        org.xml.sax.helpers.LocatorImpl sax = new org.xml.sax.helpers.LocatorImpl();
        sax.setPublicId("pub");
        sax.setSystemId("http://example.com/doc.xml");
        sax.setLineNumber(7);
        sax.setColumnNumber(3);
        javax.xml.bind.helpers.ValidationEventLocatorImpl l =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl(sax);
        check(l.getURL() != null && l.getURL().toString().equals("http://example.com/doc.xml"),
                "Locator ctor converts systemId to URL (locked)");
        check(l.getLineNumber() == 7, "Locator ctor line");
        check(l.getColumnNumber() == 3, "Locator ctor column");
        // empty SAX locator: zero line/column stay zero, null systemId -> null URL
        org.xml.sax.helpers.LocatorImpl empty = new org.xml.sax.helpers.LocatorImpl();
        javax.xml.bind.helpers.ValidationEventLocatorImpl le =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl(empty);
        check(le.getURL() == null, "Locator ctor null systemId -> null url");
        check(le.getLineNumber() == 0, "Locator ctor keeps SAX default line 0");
        check(le.getColumnNumber() == 0, "Locator ctor keeps SAX default column 0");
        check(le.getObject() == null && le.getNode() == null && le.getOffset() == -1,
                "Locator ctor leaves object/node/offset untouched");
    }

    // SAXParseException constructor maps systemId/line/column
    static void testSaxCtor() {
        org.xml.sax.SAXParseException spe =
                new org.xml.sax.SAXParseException("bad", "pub", "http://example.com/x.xml", 5, 9);
        javax.xml.bind.helpers.ValidationEventLocatorImpl l =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl(spe);
        check(l.getURL() != null && l.getURL().toString().equals("http://example.com/x.xml"),
                "SAXParseException ctor converts systemId to URL (locked)");
        check(l.getLineNumber() == 5, "SAXParseException ctor line");
        check(l.getColumnNumber() == 9, "SAXParseException ctor column");
        org.xml.sax.SAXParseException nulls =
                new org.xml.sax.SAXParseException("bad", null, null, -1, -1);
        javax.xml.bind.helpers.ValidationEventLocatorImpl ln =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl(nulls);
        check(ln.getURL() == null, "SAXParseException ctor null systemId -> null url");
        check(ln.getLineNumber() == -1, "SAXParseException ctor keeps line -1");
        check(ln.getColumnNumber() == -1, "SAXParseException ctor keeps column -1");
    }

    static void testNodeAndObjectCtors() throws Exception {
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        org.w3c.dom.Element el = doc.createElement("e");
        javax.xml.bind.helpers.ValidationEventLocatorImpl ln =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl(el);
        check(ln.getNode() == el, "Node ctor node identity");
        check(ln.getObject() == null, "Node ctor object null");
        check(ln.getLineNumber() == -1 && ln.getColumnNumber() == -1, "Node ctor line/column -1");
        Object o = new Object();
        javax.xml.bind.helpers.ValidationEventLocatorImpl lo =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl(o);
        check(lo.getObject() == o, "Object ctor object identity");
        check(lo.getNode() == null, "Object ctor node null");
    }

    static void testNegative() {
        boolean i1 = false;
        try { new javax.xml.bind.helpers.ValidationEventLocatorImpl((org.xml.sax.Locator) null); }
        catch (IllegalArgumentException e) { i1 = true; }
        check(i1, "Locator(null) throws IllegalArgumentException (locked)");
        boolean i2 = false;
        try { new javax.xml.bind.helpers.ValidationEventLocatorImpl((org.xml.sax.SAXParseException) null); }
        catch (IllegalArgumentException e) { i2 = true; }
        check(i2, "SAXParseException(null) throws IllegalArgumentException (locked)");
    }

    // toString form locked: [node=...,object=...,url=...,line=...,col=...,offset=...]
    static void testToString() throws Exception {
        javax.xml.bind.helpers.ValidationEventLocatorImpl d =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        String s = d.toString();
        check(s.contains("node=null"), "toString default node=null");
        check(s.contains("object=null"), "toString default object=null");
        check(s.contains("url=null"), "toString default url=null");
        check(s.contains("line=-1"), "toString default line=-1");
        check(s.contains("col=-1"), "toString default col=-1");
        check(s.contains("offset=-1"), "toString default offset=-1");
        javax.xml.bind.helpers.ValidationEventLocatorImpl f =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        f.setURL(new java.net.URL("http://e/x"));
        f.setOffset(11);
        f.setLineNumber(2);
        f.setColumnNumber(4);
        Object o = new Object();
        f.setObject(o);
        String fs = f.toString();
        check(fs.contains("url=http://e/x"), "toString filled url");
        check(fs.contains("line=2"), "toString filled line");
        check(fs.contains("col=4"), "toString filled col");
        check(fs.contains("offset=11"), "toString filled offset");
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        javax.xml.bind.helpers.ValidationEventLocatorImpl fn =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl(doc.createElement("e"));
        check(fn.toString().contains("node=[e"), "toString filled node");
    }
}
