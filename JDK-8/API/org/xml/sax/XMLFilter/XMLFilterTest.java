/*
 * @test
 * @summary org.xml.sax.XMLFilter conformance: interface hierarchy and methods.
 * @run main XMLFilterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLFilterTest {
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
        Class<?> c = Class.forName("org.xml.sax.XMLFilter");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.XMLReader"), "implements org.xml.sax.XMLReader");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setParent"), "has method setParent");
        check(names.contains("getParent"), "has method getParent");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral XMLFilter contract (parent chaining) ----
    static void deepContract() throws Exception {
        check(org.xml.sax.XMLReader.class.isAssignableFrom(org.xml.sax.XMLFilter.class), "XMLFilter extends XMLReader");
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader parent = spf.newSAXParser().getXMLReader();
        org.xml.sax.helpers.XMLFilterImpl filter = new org.xml.sax.helpers.XMLFilterImpl(parent);
        check(filter.getParent() == parent, "constructor wires the parent reader");
        org.xml.sax.XMLReader other = spf.newSAXParser().getXMLReader();
        filter.setParent(other);
        check(filter.getParent() == other, "setParent/getParent round-trip");
        filter.setParent(parent);
        // filter chain: parsing through the filter drives the parent and delivers events to the filter's handler
        final java.util.List<String> events = new java.util.ArrayList<String>();
        filter.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String l, String q, org.xml.sax.Attributes a) { events.add(q); }
        });
        filter.parse(new org.xml.sax.InputSource(new java.io.StringReader("<chain/>")));
        check(events.size() == 1 && events.get(0).equals("chain"), "events flow parent -> filter -> content handler");
        // no parent: parse refuses
        org.xml.sax.helpers.XMLFilterImpl orphan = new org.xml.sax.helpers.XMLFilterImpl();
        check(orphan.getParent() == null, "no-arg constructor leaves parent null");
        boolean npe = false;
        try { orphan.parse(new org.xml.sax.InputSource(new java.io.StringReader("<x/>"))); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "parse without a parent -> NullPointerException");
    }
}
