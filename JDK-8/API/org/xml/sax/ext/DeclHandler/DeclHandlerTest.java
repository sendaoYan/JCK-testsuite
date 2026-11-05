/*
 * @test
 * @summary org.xml.sax.ext.DeclHandler conformance: interface hierarchy and methods.
 * @run main DeclHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DeclHandlerTest {
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
        Class<?> c = Class.forName("org.xml.sax.ext.DeclHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("elementDecl"), "has method elementDecl");
        check(names.contains("attributeDecl"), "has method attributeDecl");
        check(names.contains("internalEntityDecl"), "has method internalEntityDecl");
        check(names.contains("externalEntityDecl"), "has method externalEntityDecl");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DeclHandler contract (DTD declaration callbacks) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final java.util.List<String> log = new java.util.ArrayList<String>();
        org.xml.sax.ext.DeclHandler dh = new org.xml.sax.ext.DeclHandler() {
            public void elementDecl(String name, String model) { log.add("elem(" + name + "=" + model + ")"); }
            public void attributeDecl(String eName, String aName, String type, String mode, String value) { log.add("att(" + aName + "," + type + "," + mode + "," + value + ")"); }
            public void internalEntityDecl(String name, String value) { log.add("ient(" + name + "=" + value + ")"); }
            public void externalEntityDecl(String name, String publicId, String systemId) { log.add("eent(" + name + ")"); }
        };
        xr.setProperty("http://xml.org/sax/properties/declaration-handler", dh);
        check(xr.getProperty("http://xml.org/sax/properties/declaration-handler") == dh, "declaration-handler property round-trip");
        String dtd = "<!DOCTYPE r [<!ELEMENT r (a*)><!ELEMENT a (#PCDATA)><!ATTLIST r x CDATA 'dx' y CDATA #REQUIRED z CDATA #FIXED 'fx'><!ENTITY ie 'iv'><!ENTITY ee SYSTEM 'e.xml'>]><r y='1'><a>t</a></r>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(dtd)));
        check(log.contains("elem(r=(a*))"), "elementDecl receives name and content model");
        check(log.contains("att(x,CDATA,null,dx)"), "defaulted attribute: attributeDecl reports value with a null mode");
        check(log.contains("att(y,CDATA,#REQUIRED,null)"), "#REQUIRED attribute: mode set, value null");
        check(log.contains("att(z,CDATA,#FIXED,fx)"), "#FIXED attribute: mode and fixed value");
        check(log.contains("ient(ie=iv)"), "internalEntityDecl receives the replacement text");
        check(log.contains("eent(ee)"), "externalEntityDecl fires for the external entity");
        // removing the handler stops the callbacks
        xr.setProperty("http://xml.org/sax/properties/declaration-handler", null);
        check(xr.getProperty("http://xml.org/sax/properties/declaration-handler") == null, "clearing the property works");
    }
}
