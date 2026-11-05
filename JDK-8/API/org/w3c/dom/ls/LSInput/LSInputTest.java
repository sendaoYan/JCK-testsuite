/*
 * @test
 * @summary org.w3c.dom.ls.LSInput conformance: reflection shape checks.
 * @run main LSInputTest
 */

import java.lang.reflect.*;

public class LSInputTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.ls.LSInput");
        check(c != null, "org.w3c.dom.ls.LSInput loads");
        check(c.getName().equals("org.w3c.dom.ls.LSInput"), "binary name is org.w3c.dom.ls.LSInput");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSInput is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSInput is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getCharacterStream"), "declares getCharacterStream");
        check(declares(c, "setCharacterStream"), "declares setCharacterStream");
        check(declares(c, "getByteStream"), "declares getByteStream");
        check(declares(c, "setByteStream"), "declares setByteStream");
        check(declares(c, "getStringData"), "declares getStringData");
        check(declares(c, "setStringData"), "declares setStringData");
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
