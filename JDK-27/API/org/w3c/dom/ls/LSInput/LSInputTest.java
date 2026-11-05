/*
 * @test
 * @summary org.w3c.dom.ls.LSInput conformance: input source.
 * @run main LSInputTest
 */

public class LSInputTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.ls.LSInput");
        check(k.isInterface(), "org.w3c.dom.ls.LSInput is an interface");
        check(k.getInterfaces().length == 0, "LSInput declares no superinterface");
        check(k.getMethod("getCharacterStream") != null, "getCharacterStream()");
        check(k.getMethod("setCharacterStream", L("java.io.Reader")) != null, "setCharacterStream(java.io.Reader)");
        check(k.getMethod("getByteStream") != null, "getByteStream()");
        check(k.getMethod("setByteStream", L("java.io.InputStream")) != null, "setByteStream(java.io.InputStream)");
        check(k.getMethod("getStringData") != null, "getStringData()");
        check(k.getMethod("setStringData", String.class) != null, "setStringData(String)");
        check(k.getMethod("getSystemId") != null, "getSystemId()");
        check(k.getMethod("getPublicId") != null, "getPublicId()");
        check(k.getMethod("getBaseURI") != null, "getBaseURI()");
        check(k.getMethod("getEncoding") != null, "getEncoding()");
        check(k.getMethod("getCertifiedText") != null, "getCertifiedText()");
        check(k.getMethod("setCertifiedText", boolean.class) != null, "setCertifiedText(boolean)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ls.LSInput a = new org.w3c.dom.ls.LSInput(){
            public java.io.Reader getCharacterStream() { return null; }
            public void setCharacterStream(java.io.Reader p0) { }
            public java.io.InputStream getByteStream() { return null; }
            public void setByteStream(java.io.InputStream p0) { }
            public java.lang.String getStringData() { return "s"; }
            public void setStringData(java.lang.String p0) { }
            public java.lang.String getSystemId() { return "s"; }
            public void setSystemId(java.lang.String p0) { }
            public java.lang.String getPublicId() { return "s"; }
            public void setPublicId(java.lang.String p0) { }
            public java.lang.String getBaseURI() { return "s"; }
            public void setBaseURI(java.lang.String p0) { }
            public java.lang.String getEncoding() { return "s"; }
            public void setEncoding(java.lang.String p0) { }
            public boolean getCertifiedText() { return true; }
            public void setCertifiedText(boolean p0) { }
        };
        check(a != null, "anon impl of LSInput");
        check(a.getCharacterStream() == null, "getCharacterStream() returns fixed default");
        a.setCharacterStream((java.io.Reader) null);
        check(a.getByteStream() == null, "getByteStream() returns fixed default");
        a.setByteStream((java.io.InputStream) null);
        check("s".equals(a.getStringData()), "getStringData() returns fixed default");
        a.setStringData("s");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        a.setSystemId("s");
        check("s".equals(a.getPublicId()), "getPublicId() returns fixed default");
        a.setPublicId("s");
        check("s".equals(a.getBaseURI()), "getBaseURI() returns fixed default");
        a.setBaseURI("s");
        check("s".equals(a.getEncoding()), "getEncoding() returns fixed default");
        a.setEncoding("s");
        check(a.getCertifiedText() == true, "getCertifiedText() returns fixed default");
        a.setCertifiedText(true);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LSInput contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument().getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSInput in = ls.createLSInput();
        check(in.getStringData() == null && in.getSystemId() == null && in.getPublicId() == null, "factory LSInput starts null");
        check(in.getEncoding() == null && in.getBaseURI() == null && !in.getCertifiedText(), "factory LSInput encoding/baseURI/certified defaults");
        in.setStringData("<q/>"); in.setSystemId("sid"); in.setPublicId("pub"); in.setBaseURI("b"); in.setEncoding("UTF-8"); in.setCertifiedText(true);
        check("<q/>".equals(in.getStringData()) && "sid".equals(in.getSystemId()) && "pub".equals(in.getPublicId()), "stringData/systemId/publicId round-trip");
        check("b".equals(in.getBaseURI()) && "UTF-8".equals(in.getEncoding()) && in.getCertifiedText(), "baseURI/encoding/certifiedText round-trip");
        in.setCertifiedText(false);
        check(!in.getCertifiedText(), "certifiedText resettable");
        // the parser really consumes stringData (write -> parse round-trip channel)
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        org.w3c.dom.ls.LSInput pin = ls.createLSInput();
        pin.setStringData("<round><x a='1'>T</x></round>");
        org.w3c.dom.Document pd = p.parse(pin);
        check("round".equals(pd.getDocumentElement().getNodeName()), "parser consumes LSInput stringData");
        check("T".equals(pd.getDocumentElement().getFirstChild().getTextContent()), "parsed content intact via LSInput");
        // user implementation is equally acceptable (duck typing)
        org.w3c.dom.ls.LSInput user = new org.w3c.dom.ls.LSInput() {
            public java.io.Reader getCharacterStream() { return new java.io.StringReader("<u/>"); }
            public void setCharacterStream(java.io.Reader r) { }
            public java.io.InputStream getByteStream() { return null; }
            public void setByteStream(java.io.InputStream b) { }
            public String getStringData() { return null; }
            public void setStringData(String d) { }
            public String getSystemId() { return null; }
            public void setSystemId(String i) { }
            public String getPublicId() { return null; }
            public void setPublicId(String i) { }
            public String getBaseURI() { return null; }
            public void setBaseURI(String i) { }
            public String getEncoding() { return null; }
            public void setEncoding(String e) { }
            public boolean getCertifiedText() { return false; }
            public void setCertifiedText(boolean c) { }
        };
        org.w3c.dom.Document ud = p.parse(user);
        check("u".equals(ud.getDocumentElement().getNodeName()), "parser consumes a user LSInput via characterStream");
    }
}
