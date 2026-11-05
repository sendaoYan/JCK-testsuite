/*
 * @test
 * @summary jdk.xml.internal.JdkXmlUtils conformance: JDK XML utils (internal).
 * @run main JdkXmlUtilsTest
 */


public class JdkXmlUtilsTest {
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
        check("true".equals(jdk.xml.internal.JdkXmlUtils.FEATURE_TRUE), "FEATURE_TRUE");
        check("false".equals(jdk.xml.internal.JdkXmlUtils.FEATURE_FALSE), "FEATURE_FALSE");
        check(jdk.xml.internal.JdkXmlUtils.getValue(5, 3) == 5, "getValue");
        check(jdk.xml.internal.JdkXmlUtils.getDOMFactory(false) != null, "getDOMFactory non-null");
        check(jdk.xml.internal.JdkXmlUtils.getDOMDocument() != null, "getDOMDocument non-null");

        // ---- constants: exact values ----
        Class<?> xu = Class.forName("jdk.xml.internal.JdkXmlUtils");
        check("http://xml.org/sax/features/namespaces".equals(xu.getField("NAMESPACES_FEATURE").get(null)), "NAMESPACES_FEATURE exact");
        check("http://xml.org/sax/features/namespace-prefixes".equals(xu.getField("NAMESPACE_PREFIXES_FEATURE").get(null)), "NAMESPACE_PREFIXES_FEATURE exact");
        check("jdk.xml.overrideDefaultParser".equals(xu.getField("OVERRIDE_PARSER").get(null)), "OVERRIDE_PARSER exact");
        check(Boolean.FALSE.equals(xu.getField("OVERRIDE_PARSER_DEFAULT").get(null)), "OVERRIDE_PARSER_DEFAULT false when unset");

        // ---- getValue(Object,int): null, Number subtypes, String, invalid type ----
        java.lang.reflect.Method getValue = xu.getMethod("getValue", Object.class, int.class);
        check(((Integer) getValue.invoke(null, null, 7)).intValue() == 7, "getValue(null,7)==7");
        check(((Integer) getValue.invoke(null, "42", 7)).intValue() == 42, "getValue('42',7)==42");
        check(((Integer) getValue.invoke(null, Long.valueOf(9L), 7)).intValue() == 9, "getValue(Long 9)==9");
        check(((Integer) getValue.invoke(null, Double.valueOf(3.9d), 7)).intValue() == 3, "getValue(Double 3.9)==3 (intValue truncation)");
        check(((Integer) getValue.invoke(null, Integer.valueOf(-1), 7)).intValue() == -1, "getValue(-1)==-1");
        try {
            getValue.invoke(null, Boolean.TRUE, 7);
            check(false, "getValue(Boolean) must throw");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof IllegalArgumentException, "getValue(Boolean) -> IllegalArgumentException");
            check(String.valueOf(ite.getCause().getMessage()).startsWith("Unexpected class"), "getValue unexpected-class message");
        }

        // ---- factory builders: namespace-aware, non-validating DOM ----
        javax.xml.parsers.DocumentBuilderFactory dbfFalse =
                (javax.xml.parsers.DocumentBuilderFactory) xu.getMethod("getDOMFactory", boolean.class).invoke(null, false);
        javax.xml.parsers.DocumentBuilderFactory dbfTrue =
                (javax.xml.parsers.DocumentBuilderFactory) xu.getMethod("getDOMFactory", boolean.class).invoke(null, true);
        check(dbfFalse != null && dbfTrue != null, "getDOMFactory both flags non-null");
        check(dbfFalse.isNamespaceAware(), "dom factory namespace aware");
        check(!dbfFalse.isValidating(), "dom factory not validating");
        check(dbfTrue.isNamespaceAware(), "dom factory(true) namespace aware");
        javax.xml.parsers.SAXParserFactory spfFalse =
                (javax.xml.parsers.SAXParserFactory) xu.getMethod("getSAXFactory", boolean.class).invoke(null, false);
        javax.xml.parsers.SAXParserFactory spfTrue =
                (javax.xml.parsers.SAXParserFactory) xu.getMethod("getSAXFactory", boolean.class).invoke(null, true);
        check(spfFalse != null && spfTrue != null, "getSAXFactory both flags non-null");
        check(spfFalse.isNamespaceAware(), "sax factory namespace aware");
        check(spfTrue.isNamespaceAware(), "sax factory(true) namespace aware");
        Object stfFalse = xu.getMethod("getSAXTransformFactory", boolean.class).invoke(null, false);
        Object stfTrue = xu.getMethod("getSAXTransformFactory", boolean.class).invoke(null, true);
        check(stfFalse instanceof javax.xml.transform.sax.SAXTransformerFactory, "getSAXTransformFactory(false) type");
        check(stfTrue instanceof javax.xml.transform.sax.SAXTransformerFactory, "getSAXTransformFactory(true) type");

        // ---- REAL PARSE LINKAGE 1: factory produced by the internal util parses in-memory XML ----
        String xml = "<root><item id='1'>alpha</item><item id='2'>beta</item></root>";
        org.w3c.dom.Document doc = dbfFalse.newDocumentBuilder().parse(
                new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        check(doc != null && doc.getDocumentElement() != null, "parse via getDOMFactory non-null");
        check("root".equals(doc.getDocumentElement().getNodeName()), "root element name");
        check(doc.getDocumentElement().getElementsByTagName("item").getLength() == 2, "two item children");
        check("2".equals(doc.getDocumentElement().getLastChild().getAttributes().getNamedItem("id").getNodeValue()), "attribute value readback");
        check("beta".equals(doc.getDocumentElement().getLastChild().getFirstChild().getNodeValue()), "text content readback");

        // ---- getXMLReader: full 2x2 matrix of (overrideDefaultParser, secureProcessing) ----
        java.lang.reflect.Method getXMLReader = xu.getMethod("getXMLReader", boolean.class, boolean.class);
        org.xml.sax.XMLReader[][] readers = new org.xml.sax.XMLReader[2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                readers[i][j] = (org.xml.sax.XMLReader) getXMLReader.invoke(null, i == 1, j == 1);
                check(readers[i][j] != null, "getXMLReader(" + (i == 1) + "," + (j == 1) + ") non-null");
                check(readers[i][j].getFeature("http://xml.org/sax/features/namespaces"), "reader namespaces on (" + (i == 1) + "," + (j == 1) + ")");
                check(!readers[i][j].getFeature("http://xml.org/sax/features/namespace-prefixes"), "reader namespace-prefixes off (" + (i == 1) + "," + (j == 1) + ")");
                check(readers[i][j].getFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING), "reader FSP feature reported on (" + (i == 1) + "," + (j == 1) + ")");
            }
        }
        check(readers[0][0] != readers[1][0], "distinct reader instances per call");

        // ---- REAL PARSE LINKAGE 2: XMLReader from the internal util parses with callbacks ----
        final int[] elems = new int[1];
        final java.util.List<String> names = new java.util.ArrayList<String>();
        org.xml.sax.XMLReader reader = readers[0][0];
        reader.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) {
                elems[0]++;
                names.add(localName);
            }
        });
        reader.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        check(elems[0] == 3, "reader parsed 3 elements");
        check(names.get(0).equals("root") && names.get(1).equals("item"), "namespace-aware local names observed");

        // ---- setXMLReaderPropertyIfSupport: supported and unsupported properties never throw ----
        java.lang.reflect.Method setProp = xu.getMethod("setXMLReaderPropertyIfSupport",
                org.xml.sax.XMLReader.class, String.class, Object.class, boolean.class);
        setProp.invoke(null, reader, "http://xml.org/sax/properties/lexical-handler", new org.xml.sax.ext.DefaultHandler2(), true);
        check(true, "setXMLReaderPropertyIfSupport supported property no throw");
        setProp.invoke(null, reader, "http://jck.bogus/property", "value", true);
        check(true, "setXMLReaderPropertyIfSupport unsupported property swallowed with warn");
        setProp.invoke(null, reader, "http://jck.bogus/property", "value", false);
        check(true, "setXMLReaderPropertyIfSupport unsupported property swallowed without warn");

        // ---- getDTDExternalDecl: PUBLIC/SYSTEM shapes and quote selection ----
        java.lang.reflect.Method dtdDecl = xu.getMethod("getDTDExternalDecl", String.class, String.class);
        check(" SYSTEM \"sys.dtd\"".equals(dtdDecl.invoke(null, null, "sys.dtd")), "system-only decl");
        check(" PUBLIC \"-//JCK//DTD\" \"sys.dtd\"".equals(dtdDecl.invoke(null, "-//JCK//DTD", "sys.dtd")), "public+system decl");
        check("".equals(dtdDecl.invoke(null, null, null)), "null ids -> empty decl");
        check(" SYSTEM 'a\"b'".equals(dtdDecl.invoke(null, null, "a\"b")), "double quote in systemId -> single-quoted");

        // ---- getDOMDocument returns a usable empty Document ----
        org.w3c.dom.Document empty = (org.w3c.dom.Document) xu.getMethod("getDOMDocument").invoke(null);
        check(empty != null && empty.getChildNodes().getLength() == 0, "getDOMDocument empty new document");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
