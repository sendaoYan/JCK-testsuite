/*
 * @test
 * @summary org.w3c.dom.ls.LSOutput conformance: output sink.
 * @run main LSOutputTest
 */

public class LSOutputTest {
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

        Class<?> k = L("org.w3c.dom.ls.LSOutput");
        check(k.isInterface(), "org.w3c.dom.ls.LSOutput is an interface");
        check(k.getInterfaces().length == 0, "LSOutput declares no superinterface");
        check(k.getMethod("getCharacterStream") != null, "getCharacterStream()");
        check(k.getMethod("setCharacterStream", L("java.io.Writer")) != null, "setCharacterStream(java.io.Writer)");
        check(k.getMethod("getByteStream") != null, "getByteStream()");
        check(k.getMethod("setByteStream", L("java.io.OutputStream")) != null, "setByteStream(java.io.OutputStream)");
        check(k.getMethod("getSystemId") != null, "getSystemId()");
        check(k.getMethod("getEncoding") != null, "getEncoding()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ls.LSOutput a = new org.w3c.dom.ls.LSOutput(){
            public java.io.Writer getCharacterStream() { return null; }
            public void setCharacterStream(java.io.Writer p0) { }
            public java.io.OutputStream getByteStream() { return null; }
            public void setByteStream(java.io.OutputStream p0) { }
            public java.lang.String getSystemId() { return "s"; }
            public void setSystemId(java.lang.String p0) { }
            public java.lang.String getEncoding() { return "s"; }
            public void setEncoding(java.lang.String p0) { }
        };
        check(a != null, "anon impl of LSOutput");
        check(a.getCharacterStream() == null, "getCharacterStream() returns fixed default");
        a.setCharacterStream((java.io.Writer) null);
        check(a.getByteStream() == null, "getByteStream() returns fixed default");
        a.setByteStream((java.io.OutputStream) null);
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        a.setSystemId("s");
        check("s".equals(a.getEncoding()), "getEncoding() returns fixed default");
        a.setEncoding("s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LSOutput contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument().getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSOutput out = ls.createLSOutput();
        check(out.getSystemId() == null && out.getEncoding() == null && out.getByteStream() == null && out.getCharacterStream() == null, "factory LSOutput starts null");
        out.setSystemId("osid"); out.setEncoding("UTF-8");
        check("osid".equals(out.getSystemId()) && "UTF-8".equals(out.getEncoding()), "systemId/encoding round-trip");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        out.setByteStream(bos);
        check(out.getByteStream() == bos, "byteStream round-trip");
        java.io.StringWriter sw = new java.io.StringWriter();
        out.setCharacterStream(sw);
        check(out.getCharacterStream() == sw, "characterStream round-trip");
        // LSSerializer.write consumes a user LSOutput and reports success
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        org.w3c.dom.Element r = d.createElement("w"); d.appendChild(r); r.appendChild(d.createTextNode("hi"));
        org.w3c.dom.ls.LSSerializer ser = ls.createLSSerializer();
        boolean wrote = ser.write(d, out);
        check(wrote && sw.toString().contains("<w>hi</w>"), "LSSerializer.write fills the wired characterStream");
        check(ser.writeToString(d).contains("<w>hi</w>"), "writeToString agrees with the streamed output");
    }
}
