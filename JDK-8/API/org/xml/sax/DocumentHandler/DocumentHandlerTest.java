/*
 * @test
 * @summary org.xml.sax.DocumentHandler conformance: interface hierarchy and methods.
 * @run main DocumentHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DocumentHandlerTest {
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
        Class<?> c = Class.forName("org.xml.sax.DocumentHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setDocumentLocator"), "has method setDocumentLocator");
        check(names.contains("startDocument"), "has method startDocument");
        check(names.contains("endDocument"), "has method endDocument");
        check(names.contains("startElement"), "has method startElement");
        check(names.contains("endElement"), "has method endElement");
        check(names.contains("characters"), "has method characters");
        check(names.contains("ignorableWhitespace"), "has method ignorableWhitespace");
        check(names.contains("processingInstruction"), "has method processingInstruction");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DocumentHandler contract (SAX1 event set) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.Parser p1 = spf.newSAXParser().getParser();
        final java.util.List<String> log = new java.util.ArrayList<String>();
        p1.setDocumentHandler(new org.xml.sax.DocumentHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) { log.add("locator"); }
            public void startDocument() { log.add("start"); }
            public void endDocument() { log.add("end"); }
            public void startElement(String name, org.xml.sax.AttributeList a) { log.add("se(" + name + "#" + a.getLength() + ")"); }
            public void endElement(String name) { log.add("ee(" + name + ")"); }
            public void characters(char[] c, int s, int n) { log.add("ch(" + new String(c, s, n).trim() + ")"); }
            public void ignorableWhitespace(char[] c, int s, int n) { log.add("ws"); }
            public void processingInstruction(String t, String d) { log.add("pi(" + t + ")"); }
        });
        p1.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r x='1'>T</r>")));
        check(log.get(0).equals("locator") && log.get(1).equals("start"), "SAX1 order: locator then startDocument");
        check(log.contains("se(r#1)") && log.contains("ch(T)") && log.contains("ee(r)"), "SAX1 element/characters/end sequence with attribute count");
        check(log.get(log.size() - 1).equals("end"), "endDocument terminates the SAX1 stream");
        check(org.xml.sax.DocumentHandler.class.isInterface(), "DocumentHandler is an interface");
    }
}
