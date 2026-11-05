/*
 * @test
 * @summary org.w3c.dom.ls.LSSerializer conformance: serializer.
 * @run main LSSerializerTest
 */

public class LSSerializerTest {
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

        Class<?> k = L("org.w3c.dom.ls.LSSerializer");
        check(k.isInterface(), "org.w3c.dom.ls.LSSerializer is an interface");
        check(k.getInterfaces().length == 0, "LSSerializer declares no superinterface");
        check(k.getMethod("getDomConfig") != null, "getDomConfig()");
        check(k.getMethod("getNewLine") != null, "getNewLine()");
        check(k.getMethod("setNewLine", String.class) != null, "setNewLine(String)");
        check(k.getMethod("getFilter") != null, "getFilter()");
        check(k.getMethod("setFilter", L("org.w3c.dom.ls.LSSerializerFilter")) != null, "setFilter(org.w3c.dom.ls.LSSerializerFilter)");
        check(k.getMethod("write", L("org.w3c.dom.Node"), L("org.w3c.dom.ls.LSOutput")) != null, "write(org.w3c.dom.Node, org.w3c.dom.ls.LSOutput)");
        check(k.getMethod("writeToURI", L("org.w3c.dom.Node"), String.class) != null, "writeToURI(org.w3c.dom.Node, String)");
        check(k.getMethod("writeToString", L("org.w3c.dom.Node")) != null, "writeToString(org.w3c.dom.Node)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ls.LSSerializer a = new org.w3c.dom.ls.LSSerializer(){
            public org.w3c.dom.DOMConfiguration getDomConfig() { return null; }
            public java.lang.String getNewLine() { return "s"; }
            public void setNewLine(java.lang.String p0) { }
            public org.w3c.dom.ls.LSSerializerFilter getFilter() { return null; }
            public void setFilter(org.w3c.dom.ls.LSSerializerFilter p0) { }
            public boolean write(org.w3c.dom.Node p0, org.w3c.dom.ls.LSOutput p1) { return true; }
            public boolean writeToURI(org.w3c.dom.Node p0, java.lang.String p1) { return true; }
            public java.lang.String writeToString(org.w3c.dom.Node p0) { return "s"; }
        };
        check(a != null, "anon impl of LSSerializer");
        check(a.getDomConfig() == null, "getDomConfig() returns fixed default");
        check("s".equals(a.getNewLine()), "getNewLine() returns fixed default");
        a.setNewLine("s");
        check(a.getFilter() == null, "getFilter() returns fixed default");
        a.setFilter((org.w3c.dom.ls.LSSerializerFilter) null);
        check(a.write((org.w3c.dom.Node) null, (org.w3c.dom.ls.LSOutput) null) == true, "write() returns fixed default");
        check(a.writeToURI((org.w3c.dom.Node) null, "s") == true, "writeToURI() returns fixed default");
        check("s".equals(a.writeToString((org.w3c.dom.Node) null)), "writeToString() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LSSerializer contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element r = d.createElement("r"); d.appendChild(r);
        org.w3c.dom.Element a = d.createElement("a"); a.setAttribute("x", "1"); a.appendChild(d.createTextNode("T"));
        r.appendChild(a); r.appendChild(d.createElement("b"));
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) d.getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSSerializer ser = ls.createLSSerializer();
        String xml = ser.writeToString(d);
        check(xml.startsWith("<?xml version=\"1.0\""), "writeToString emits an XML declaration");
        check(xml.contains("<a x=\"1\">T</a>") && xml.contains("<b/>"), "writeToString serializes attributes and text");
        check((Object) d.getDocumentElement().getChildNodes().getLength() == (Object) 2, "serialization leaves the source tree untouched");
        // write -> parse round-trip (LS write/read closed loop)
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        org.w3c.dom.ls.LSInput in = ls.createLSInput(); in.setStringData(xml);
        org.w3c.dom.Document back = p.parse(in);
        check("r".equals(back.getDocumentElement().getNodeName()), "write->parse round-trip recovers the root");
        check("T".equals(back.getDocumentElement().getFirstChild().getTextContent()) && back.getDocumentElement().getFirstChild().getAttributes().getNamedItem("x").getNodeValue().equals("1"), "round-trip preserves attributes and text");
        // write(LSOutput) equals writeToString
        java.io.StringWriter sw = new java.io.StringWriter();
        org.w3c.dom.ls.LSOutput out = ls.createLSOutput(); out.setCharacterStream(sw);
        boolean is8 = System.getProperty("java.version").startsWith("1.8");
        check(ser.write(d, out), "write(LSOutput) reports success");
        check(sw.toString().substring(sw.toString().indexOf("?>") + 2).equals(xml.substring(xml.indexOf("?>") + 2)), "write(LSOutput) serializes the same body as writeToString");
        check(xml.contains("UTF-16"), "writeToString on a built-from-scratch document declares UTF-16");
        if (is8) check(sw.toString().contains("UTF-8"), "JDK 8: write(LSOutput) without an encoding declares UTF-8");
        else check(sw.toString().contains("UTF-16"), "JDK 27: write(LSOutput) without an encoding stays UTF-16");
        // newLine handling
        check(ser.getNewLine() != null, "default newLine is a concrete value on this implementation");
        ser.setNewLine("\r\n");
        check("\r\n".equals(ser.getNewLine()), "setNewLine round-trip");
        // pretty printing
        check(ser.getDomConfig().canSetParameter("format-pretty-print", Boolean.TRUE), "canSetParameter(format-pretty-print)");
        ser.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        String pretty = ser.writeToString(d);
        check(pretty.contains("\n    <a x=\"1\">T</a>"), "pretty-print indents children with four spaces");
        // writeToURI writes a real file on JDK 8; JDK 27 Xerces throws LSException(NPE charsetName)
        java.io.File tmp = java.io.File.createTempFile("lsser", ".xml");
        tmp.deleteOnExit();
        if (is8) {
            check(ser.writeToURI(d, tmp.toURI().toString()), "JDK 8: writeToURI reports success");
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(tmp));
            String first = br.readLine(); br.close();
            check(first != null && first.contains("<?xml"), "JDK 8: written file starts with the XML declaration");
        } else {
            try { ser.writeToURI(d, tmp.toURI().toString()); check(false, "JDK 27: writeToURI must fail"); }
            catch (org.w3c.dom.ls.LSException ex) { check(true, "JDK 27: writeToURI -> LSException (NPE charsetName)"); }
        }
        // declaration-newline difference between the two JDKs (Xerces versions)
        String flat = ls.createLSSerializer().writeToString(d);
        if (is8) check(flat.contains("?>\n<r>"), "JDK 8: newline after the declaration in flat output");
        else check(!flat.contains("?>\n") && flat.contains("?><r>"), "JDK 27: declaration immediately followed by the root");
    }
}
