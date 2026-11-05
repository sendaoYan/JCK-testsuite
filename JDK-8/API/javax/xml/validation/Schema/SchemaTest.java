/*
 * @test
 * @summary javax.xml.validation.Schema conformance: abstract class hierarchy and methods.
 * @run main SchemaTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SchemaTest {
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
        Class<?> c = Class.forName("javax.xml.validation.Schema");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("newValidator"), "has method newValidator");
        check(names.contains("newValidatorHandler"), "has method newValidatorHandler");
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static final String XSD =
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
      + "<xs:element name='root'>"
      + "<xs:complexType>"
      + "<xs:sequence>"
      + "<xs:element name='name' type='xs:string' minOccurs='1'/>"
      + "<xs:element name='count' type='xs:int' minOccurs='1'/>"
      + "</xs:sequence>"
      + "<xs:attribute name='id' type='xs:int' use='required'/>"
      + "</xs:complexType>"
      + "</xs:element>"
      + "</xs:schema>";

    static final String BAD_DOC = "<root id='7'><count>42</count></root>";

    static void testBehavior() throws Exception {
        javax.xml.validation.SchemaFactory f =
            javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        javax.xml.validation.Schema s =
            f.newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD)));

        // ---- newValidator: multiple distinct independent instances ----
        javax.xml.validation.Validator v1 = s.newValidator();
        javax.xml.validation.Validator v2 = s.newValidator();
        check(v1 != null && v2 != null, "newValidator non-null");
        check(v1 != v2, "newValidator returns fresh instance each call");

        // v1 gets a recording handler -> invalid doc reported via callback, validate returns
        final int[] errCalls = {0};
        v1.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void error(org.xml.sax.SAXParseException e) { errCalls[0]++; }
        });
        v1.validate(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(BAD_DOC)));
        check(true, "v1 with error handler: validate returns normally");
        check(errCalls[0] == 1, "v1 error handler invoked exactly once");

        // v2 has no handler -> same document throws (handler state not shared)
        boolean threw = false;
        try {
            v2.validate(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(BAD_DOC)));
        } catch (org.xml.sax.SAXParseException e) { threw = true; }
        check(threw, "v2 (separate instance) still throws: newValidator instances independent");
        check(v2.getErrorHandler() == null, "v2 error handler untouched by v1");

        // ---- newValidatorHandler: distinct, independent of validators ----
        javax.xml.validation.ValidatorHandler vh1 = s.newValidatorHandler();
        javax.xml.validation.ValidatorHandler vh2 = s.newValidatorHandler();
        check(vh1 != null && vh2 != null, "newValidatorHandler non-null");
        check(vh1 != vh2, "newValidatorHandler returns fresh instance each call");
        org.xml.sax.ContentHandler ch = new org.xml.sax.helpers.DefaultHandler();
        vh1.setContentHandler(ch);
        check(vh1.getContentHandler() == ch, "vh1 content handler round-trip");
        check(vh2.getContentHandler() == null, "vh2 content handler untouched by vh1");
    }
}
