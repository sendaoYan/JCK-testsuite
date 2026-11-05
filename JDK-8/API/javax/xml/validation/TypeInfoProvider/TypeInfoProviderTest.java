/*
 * @test
 * @summary javax.xml.validation.TypeInfoProvider conformance: abstract class hierarchy and methods.
 * @run main TypeInfoProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class TypeInfoProviderTest {
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
        Class<?> c = Class.forName("javax.xml.validation.TypeInfoProvider");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getElementTypeInfo"), "has method getElementTypeInfo");
        check(names.contains("getAttributeTypeInfo"), "has method getAttributeTypeInfo");
        check(names.contains("isIdAttribute"), "has method isIdAttribute");
        check(names.contains("isSpecified"), "has method isSpecified");
        // constructor is protected (abstract class, not directly instantiable)
        check(c.getConstructors().length == 0, "no public constructors");
        testRealProvider();
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

    // Real TypeInfoProvider obtained from a ValidatorHandler during a validating parse.
    static void testRealProvider() throws Exception {
        javax.xml.validation.SchemaFactory f =
            javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        javax.xml.validation.Schema s =
            f.newSchema(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSD)));
        javax.xml.validation.ValidatorHandler vh = s.newValidatorHandler();
        javax.xml.validation.TypeInfoProvider tip = vh.getTypeInfoProvider();
        check(tip != null, "real TypeInfoProvider from ValidatorHandler");

        final javax.xml.validation.TypeInfoProvider t = tip;
        final String[][] captured = new String[4][2]; // [element index] {typeName, typeNamespace}
        vh.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            int idx = 0;
            public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attrs) {
                org.w3c.dom.TypeInfo ti = t.getElementTypeInfo();
                captured[idx][0] = ti == null ? null : ti.getTypeName();
                captured[idx][1] = ti == null ? null : ti.getTypeNamespace();
                idx++;
            }
        });
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setContentHandler(vh);
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(
            "<root id='7'><name>abc</name><count>42</count></root>")));

        check(captured[0][0] != null, "root: anonymous type name non-null");
        check(captured[0][1] == null || captured[0][1].isEmpty(),
              "root: anonymous type has null/empty namespace (got " + captured[0][1] + ")");
        check("string".equals(captured[1][0]), "name element declared type xs:string");
        check("http://www.w3.org/2001/XMLSchema".equals(captured[1][1]), "name type namespace is XSD ns");
        check("int".equals(captured[2][0]), "count element declared type xs:int");
        check("http://www.w3.org/2001/XMLSchema".equals(captured[2][1]), "count type namespace is XSD ns");

        // custom subclass dispatch (contract of the abstract methods)
        final org.w3c.dom.TypeInfo fixed = new org.w3c.dom.TypeInfo() {
            public String getTypeName() { return "fixed"; }
            public String getTypeNamespace() { return "urn:fixed"; }
            public boolean isDerivedFrom(String ns, String ln, int method) { return false; }
        };
        javax.xml.validation.TypeInfoProvider custom = new javax.xml.validation.TypeInfoProvider() {
            public org.w3c.dom.TypeInfo getElementTypeInfo() { return fixed; }
            public org.w3c.dom.TypeInfo getAttributeTypeInfo(int i) { return fixed; }
            public boolean isIdAttribute(int i) { return true; }
            public boolean isSpecified(int i) { return false; }
        };
        check(custom.getElementTypeInfo() == fixed, "subclass getElementTypeInfo dispatch");
        check(custom.getAttributeTypeInfo(2) == fixed, "subclass getAttributeTypeInfo dispatch");
        check(custom.isIdAttribute(0), "subclass isIdAttribute dispatch");
        check(!custom.isSpecified(0), "subclass isSpecified dispatch");
    }
}
