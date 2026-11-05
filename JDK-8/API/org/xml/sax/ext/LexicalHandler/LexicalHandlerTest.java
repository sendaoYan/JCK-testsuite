/*
 * @test
 * @summary org.xml.sax.ext.LexicalHandler conformance: interface hierarchy and methods.
 * @run main LexicalHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class LexicalHandlerTest {
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
        Class<?> c = Class.forName("org.xml.sax.ext.LexicalHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("startDTD"), "has method startDTD");
        check(names.contains("endDTD"), "has method endDTD");
        check(names.contains("startEntity"), "has method startEntity");
        check(names.contains("endEntity"), "has method endEntity");
        check(names.contains("startCDATA"), "has method startCDATA");
        check(names.contains("endCDATA"), "has method endCDATA");
        check(names.contains("comment"), "has method comment");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LexicalHandler contract (DTD/CDATA/entity/comment events) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final java.util.List<String> log = new java.util.ArrayList<String>();
        org.xml.sax.ext.LexicalHandler lh = new org.xml.sax.ext.LexicalHandler() {
            public void startDTD(String name, String pid, String sid) { log.add("startDTD(" + name + "," + pid + "," + sid + ")"); }
            public void endDTD() { log.add("endDTD"); }
            public void startEntity(String name) { log.add("startEnt(" + name + ")"); }
            public void endEntity(String name) { log.add("endEnt(" + name + ")"); }
            public void startCDATA() { log.add("startCDATA"); }
            public void endCDATA() { log.add("endCDATA"); }
            public void comment(char[] c, int s, int n) { log.add("comment(" + new String(c, s, n) + ")"); }
        };
        xr.setProperty("http://xml.org/sax/properties/lexical-handler", lh);
        check(xr.getProperty("http://xml.org/sax/properties/lexical-handler") == lh, "lexical-handler property round-trip");
        String xml = "<!DOCTYPE r [<!ENTITY ie 'IV'>]><r><!--dc--><![CDATA[cd]]>&ie;<!--tail--></r>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        check(log.get(0).equals("startDTD(r,null,null)"), "startDTD receives name/publicId/systemId");
        check(log.contains("endDTD"), "endDTD pairs with startDTD");
        check(log.contains("comment(dc)") && log.contains("comment(tail)"), "comments inside and after content both reported");
        int sc = log.indexOf("startCDATA");
        int ec = log.indexOf("endCDATA");
        check(sc >= 0 && ec > sc, "startCDATA/endCDATA bracket the CDATA content");
        check(log.contains("startEnt(ie)") && log.contains("endEnt(ie)"), "internal entity expansion wrapped in startEntity/endEntity");
        // comments also appear in the DTD
        final int[] dtdComments = new int[]{0};
        org.xml.sax.ext.LexicalHandler lh2 = new org.xml.sax.ext.LexicalHandler() {
            public void startDTD(String n, String p, String s) { dtdComments[0] = 1; }
            public void endDTD() { }
            public void startEntity(String n) { }
            public void endEntity(String n) { }
            public void startCDATA() { }
            public void endCDATA() { }
            public void comment(char[] c, int s2, int n) { if (dtdComments[0] == 1) dtdComments[0] = 2; }
        };
        org.xml.sax.XMLReader xr2 = spf.newSAXParser().getXMLReader();
        xr2.setProperty("http://xml.org/sax/properties/lexical-handler", lh2);
        xr2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!--dtdc--><!ELEMENT r (#PCDATA)>]><r/>")));
        check(dtdComments[0] == 2, "comments inside the internal DTD subset are reported");
    }
}
