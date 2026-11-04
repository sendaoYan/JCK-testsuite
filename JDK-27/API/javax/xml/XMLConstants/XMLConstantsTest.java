/*
 * @test
 * @summary javax.xml.XMLConstants conformance: all public constant values.
 * @run main XMLConstantsTest
 */

import javax.xml.XMLConstants;

public class XMLConstantsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check("".equals(XMLConstants.NULL_NS_URI), "NULL_NS_URI == \"\"");
        check("".equals(XMLConstants.DEFAULT_NS_PREFIX), "DEFAULT_NS_PREFIX == \"\"");
        check("http://www.w3.org/XML/1998/namespace".equals(XMLConstants.XML_NS_URI), "XML_NS_URI");
        check("xml".equals(XMLConstants.XML_NS_PREFIX), "XML_NS_PREFIX == xml");
        check("http://www.w3.org/2000/xmlns/".equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI), "XMLNS_ATTRIBUTE_NS_URI");
        check("xmlns".equals(XMLConstants.XMLNS_ATTRIBUTE), "XMLNS_ATTRIBUTE == xmlns");
        check("http://www.w3.org/2001/XMLSchema".equals(XMLConstants.W3C_XML_SCHEMA_NS_URI), "W3C_XML_SCHEMA_NS_URI");
        check("http://www.w3.org/2001/XMLSchema-instance".equals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI), "W3C_XML_SCHEMA_INSTANCE_NS_URI");
        check("http://www.w3.org/2003/11/xpath-datatypes".equals(XMLConstants.W3C_XPATH_DATATYPE_NS_URI), "W3C_XPATH_DATATYPE_NS_URI");
        check("http://www.w3.org/TR/REC-xml".equals(XMLConstants.XML_DTD_NS_URI), "XML_DTD_NS_URI");
        check("http://relaxng.org/ns/structure/1.0".equals(XMLConstants.RELAXNG_NS_URI), "RELAXNG_NS_URI");
        check("http://javax.xml.XMLConstants/feature/secure-processing".equals(XMLConstants.FEATURE_SECURE_PROCESSING), "FEATURE_SECURE_PROCESSING");
        check("http://javax.xml.XMLConstants/property/accessExternalDTD".equals(XMLConstants.ACCESS_EXTERNAL_DTD), "ACCESS_EXTERNAL_DTD");
        check("http://javax.xml.XMLConstants/property/accessExternalSchema".equals(XMLConstants.ACCESS_EXTERNAL_SCHEMA), "ACCESS_EXTERNAL_SCHEMA");
        check("http://javax.xml.XMLConstants/property/accessExternalStylesheet".equals(XMLConstants.ACCESS_EXTERNAL_STYLESHEET), "ACCESS_EXTERNAL_STYLESHEET");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
