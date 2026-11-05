/*
 * @test
 * @summary org.xml.sax.DTDHandler conformance: interface hierarchy and methods.
 * @run main DTDHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DTDHandlerTest {
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
        Class<?> c = Class.forName("org.xml.sax.DTDHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("notationDecl"), "has method notationDecl");
        check(names.contains("unparsedEntityDecl"), "has method unparsedEntityDecl");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DTDHandler contract (notation + unparsed entity) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final java.util.List<String> log = new java.util.ArrayList<String>();
        xr.setDTDHandler(new org.xml.sax.DTDHandler() {
            public void notationDecl(String name, String publicId, String systemId) { log.add("notation(" + name + "," + publicId + ")"); }
            public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) { log.add("entity(" + name + "," + notationName + ")"); }
        });
        String xml = "<!DOCTYPE r [<!NOTATION gif SYSTEM 'gif.exe'><!ENTITY pic SYSTEM 'p.gif' NDATA gif>]><r/>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        check(log.size() == 2, "one notation + one unparsed entity declaration reported");
        check(log.get(0).startsWith("notation(gif,"), "notationDecl receives name/publicId");
        check(log.get(1).equals("entity(pic,gif)"), "unparsedEntityDecl receives name and NDATA notation");
        check(xr.getDTDHandler() != null, "getDTDHandler round-trip");
    }
}
