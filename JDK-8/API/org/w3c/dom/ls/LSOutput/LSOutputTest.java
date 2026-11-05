/*
 * @test
 * @summary org.w3c.dom.ls.LSOutput conformance: reflection shape checks.
 * @run main LSOutputTest
 */

import java.lang.reflect.*;

public class LSOutputTest {
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
        Class<?> c = Class.forName("org.w3c.dom.ls.LSOutput");
        check(c != null, "org.w3c.dom.ls.LSOutput loads");
        check(c.getName().equals("org.w3c.dom.ls.LSOutput"), "binary name is org.w3c.dom.ls.LSOutput");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSOutput is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSOutput is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getCharacterStream"), "declares getCharacterStream");
        check(declares(c, "setCharacterStream"), "declares setCharacterStream");
        check(declares(c, "getByteStream"), "declares getByteStream");
        check(declares(c, "setByteStream"), "declares setByteStream");
        check(declares(c, "getSystemId"), "declares getSystemId");
        check(declares(c, "setSystemId"), "declares setSystemId");
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
