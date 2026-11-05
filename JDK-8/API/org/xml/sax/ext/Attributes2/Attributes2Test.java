/*
 * @test
 * @summary org.xml.sax.ext.Attributes2 conformance: interface hierarchy and methods.
 * @run main Attributes2Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class Attributes2Test {
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
        Class<?> c = Class.forName("org.xml.sax.ext.Attributes2");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.Attributes"), "implements org.xml.sax.Attributes");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isDeclared"), "has method isDeclared");
        check(names.contains("isSpecified"), "has method isSpecified");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Attributes2 contract (live isSpecified/isDeclared during parse) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final java.util.List<String> log = new java.util.ArrayList<String>();
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String uri, String ln, String q, org.xml.sax.Attributes a) {
                if (!(a instanceof org.xml.sax.ext.Attributes2)) { log.add("NOT-A2"); return; }
                org.xml.sax.ext.Attributes2 a2 = (org.xml.sax.ext.Attributes2) a;
                for (int i = 0; i < a.getLength(); i++)
                    log.add(a.getQName(i) + ":" + (a2.isSpecified(i) ? "T" : "F") + (a2.isDeclared(i) ? "D" : "U"));
            }
        });
        String dtd = "<!DOCTYPE r [<!ATTLIST r d CDATA 'dv'><!ELEMENT r (#PCDATA)>]><r d='explicit' e='plain'/>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(dtd)));
        check(log.contains("d:TD"), "explicit attribute: specified=T declared=T");
        check(log.contains("e:TU"), "undeclared attribute: specified=T declared=F");
        // DTD-defaulted attribute: present with default value, specified=false
        final java.util.List<String> log2 = new java.util.ArrayList<String>();
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String uri, String ln, String q, org.xml.sax.Attributes a) {
                org.xml.sax.ext.Attributes2 a2 = (org.xml.sax.ext.Attributes2) a;
                for (int i = 0; i < a.getLength(); i++)
                    log2.add(a.getQName(i) + "=" + a.getValue(i) + ":" + (a2.isSpecified(i) ? "T" : "F") + (a2.isDeclared(i) ? "D" : "U"));
            }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!ATTLIST r d CDATA 'dv'><!ELEMENT r (#PCDATA)>]><r/>")));
        check(log2.contains("d=dv:FD"), "DTD default attribute: value=dv, specified=false, declared=true");
        check(org.xml.sax.Attributes.class.isAssignableFrom(org.xml.sax.ext.Attributes2.class), "Attributes2 extends org.xml.sax.Attributes");
    }
}
