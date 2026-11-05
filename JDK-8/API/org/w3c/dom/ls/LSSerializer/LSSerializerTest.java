/*
 * @test
 * @summary org.w3c.dom.ls.LSSerializer conformance: reflection shape checks.
 * @run main LSSerializerTest
 */

import java.lang.reflect.*;

public class LSSerializerTest {
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
        Class<?> c = Class.forName("org.w3c.dom.ls.LSSerializer");
        check(c != null, "org.w3c.dom.ls.LSSerializer loads");
        check(c.getName().equals("org.w3c.dom.ls.LSSerializer"), "binary name is org.w3c.dom.ls.LSSerializer");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSSerializer is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSSerializer is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getDomConfig"), "declares getDomConfig");
        check(declares(c, "getNewLine"), "declares getNewLine");
        check(declares(c, "setNewLine"), "declares setNewLine");
        check(declares(c, "getFilter"), "declares getFilter");
        check(declares(c, "setFilter"), "declares setFilter");
        check(declares(c, "write"), "declares write");
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
