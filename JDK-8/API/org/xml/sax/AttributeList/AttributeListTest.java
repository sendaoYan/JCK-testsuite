/*
 * @test
 * @summary org.xml.sax.AttributeList conformance: interface hierarchy and methods.
 * @run main AttributeListTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttributeListTest {
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
        Class<?> c = Class.forName("org.xml.sax.AttributeList");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLength"), "has method getLength");
        check(names.contains("getName"), "has method getName");
        check(names.contains("getType"), "has method getType");
        check(names.contains("getValue"), "has method getValue");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral AttributeList contract (SAX1 channel) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.Parser p1 = spf.newSAXParser().getParser();
        check(p1 != null, "SAXParser.getParser() yields a live SAX1 Parser");
        final org.xml.sax.AttributeList[] seen = new org.xml.sax.AttributeList[1];
        final int[] len = new int[]{-1};
        p1.setDocumentHandler(new org.xml.sax.HandlerBase() {
            public void startElement(String name, org.xml.sax.AttributeList atts) { seen[0] = atts; len[0] = atts.getLength(); }
        });
        p1.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r x='1' y='v'>T</r>")));
        org.xml.sax.AttributeList a = seen[0];
        check(len[0] == 2, "SAX1 AttributeList carries both attributes");
        check("x".equals(a.getName(0)) && "y".equals(a.getName(1)), "getName in document order");
        check("1".equals(a.getValue(0)) && "v".equals(a.getValue(1)), "getValue by index");
        check("1".equals(a.getValue("x")), "getValue by name");
        check("CDATA".equals(a.getType(0)) && "CDATA".equals(a.getType("x")), "getType CDATA");
        check(a.getValue("zz") == null && a.getType("zz") == null, "missing name -> null");
        check(a.getName(9) == null && a.getValue(9) == null && a.getType(9) == null, "out-of-range index -> null");
        // AttributesImpl -> AttributeList view via ParserAdapter channel (helpers covered separately)
        check(org.xml.sax.AttributeList.class.isInterface(), "AttributeList is an interface");
    }
}
