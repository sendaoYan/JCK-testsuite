/*
 * @test
 * @summary javax.xml.parsers.DocumentBuilderFactory conformance: factory boundary, precise exceptions, null semantics, configuration round-trip.
 * @run main DocumentBuilderFactoryTest
 */

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.StringReader;

public class DocumentBuilderFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isFce(Thrower t) {
        try { t.run(); return false; }
        catch (FactoryConfigurationError e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isPce(Thrower t) {
        try { t.run(); return false; }
        catch (ParserConfigurationException e) { return true; }
        catch (Exception e) { return false; }
    }

    public static void main(String[] args) throws Exception {
        // ---- factory boundary ----
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        check(f != null, "newInstance non-null");

        // explicit impl class loads via the bootstrap-aware overload
        ClassLoader cl = DocumentBuilderFactory.class.getClassLoader();
        DocumentBuilderFactory f2 = DocumentBuilderFactory.newInstance(
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", cl);
        check(f2 != null, "newInstance(className, loader) non-null");

        // ---- precise factory exceptions ----
        check(isFce(() -> DocumentBuilderFactory.newInstance("no.such.Factory", cl)),
              "bogus impl class -> FactoryConfigurationError");
        check(isFce(() -> DocumentBuilderFactory.newInstance("no.such.Factory", null)),
              "bogus impl class null loader -> FactoryConfigurationError");
        check(isFce(() -> DocumentBuilderFactory.newInstance(null, null)),
              "null factory class -> FactoryConfigurationError");

        // ---- documented defaults ----
        check(!f.isNamespaceAware(), "default namespaceAware=false");
        check(!f.isValidating(), "default validating=false");
        check(!f.isXIncludeAware(), "default xIncludeAware=false");
        check(f.getSchema() == null, "default schema=null");

        // ---- configuration round-trip ----
        f.setNamespaceAware(true);  check(f.isNamespaceAware(), "namespaceAware=true round-trip");
        f.setNamespaceAware(false); check(!f.isNamespaceAware(), "namespaceAware=false round-trip");
        f.setValidating(true);      check(f.isValidating(), "validating=true round-trip");
        f.setValidating(false);     check(!f.isValidating(), "validating=false round-trip");
        f.setXIncludeAware(true);   check(f.isXIncludeAware(), "xIncludeAware=true round-trip");
        f.setXIncludeAware(false);  check(!f.isXIncludeAware(), "xIncludeAware=false round-trip");
        f.setIgnoringElementContentWhitespace(true);
        check(f.isIgnoringElementContentWhitespace(), "ignoringWhitespace round-trip");
        f.setExpandEntityReferences(true);
        check(f.isExpandEntityReferences(), "expandEntityReferences round-trip");
        f.setIgnoringComments(true);
        check(f.isIgnoringComments(), "ignoringComments round-trip");
        f.setCoalescing(true);
        check(f.isCoalescing(), "coalescing round-trip");

        // setSchema(null) is a legal way to clear schema
        f.setSchema(null);
        check(f.getSchema() == null, "setSchema(null) clears schema");

        // ---- attribute null semantics / exceptions ----
        check(isIae(() -> f.setAttribute(null, "v")), "setAttribute(null,..) -> IAE");
        check(isNpe(() -> f.getAttribute(null)), "getAttribute(null) -> NPE");
        // unknown attribute name on the JDK impl -> IllegalArgumentException
        check(isIae(() -> f.getAttribute("http://no.such.attribute")), "getAttribute(unknown) -> IAE");
        check(isIae(() -> f.setAttribute("http://no.such.attribute", "v")), "setAttribute(unknown) -> IAE");

        // ---- feature null semantics / exceptions ----
        check(isPce(() -> f.setFeature("http://no.such/feature", true)), "setFeature(unknown) -> ParserConfigurationException");
        check(isPce(() -> f.getFeature("http://no.such/feature")), "getFeature(unknown) -> ParserConfigurationException");
        check(isNpe(() -> f.setFeature(null, true)), "setFeature(null,..) -> NPE");

        // ---- builder production ----
        DocumentBuilder b = f.newDocumentBuilder();
        check(b != null, "newDocumentBuilder non-null");
        DocumentBuilder b2 = f.newDocumentBuilder();
        check(b2 != null && b2 != b, "newDocumentBuilder returns fresh builder");

        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    // JDK-8: with expandEntityReferences=false the root's getTextContent still expands the entity.
    // JDK-27: it does not (empty). Each side locks its own observed value.
    static final String EXPAND_ER_ROOT_TEXT = "expanded";

    static Document parse(DocumentBuilderFactory f, String xml) throws Exception {
        return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    static void testDeep() throws Exception {
        // ---- coalescing behavior matrix ----
        DocumentBuilderFactory cf = DocumentBuilderFactory.newInstance();
        cf.setCoalescing(true);
        Document dc = parse(cf, "<r>a<![CDATA[b]]>c</r>");
        Node rc = dc.getDocumentElement();
        check(rc.getChildNodes().getLength() == 1, "coalescing=true merges text+CDATA into one node");
        check(rc.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE, "coalesced node is TEXT (3)");
        check(rc.getTextContent().equals("abc"), "coalesced content");
        Document dnc = parse(DocumentBuilderFactory.newInstance(), "<r>a<![CDATA[b]]>c</r>");
        Node rnc = dnc.getDocumentElement();
        check(rnc.getChildNodes().getLength() == 3, "coalescing=false keeps three nodes");
        check(rnc.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE
           && rnc.getChildNodes().item(1).getNodeType() == Node.CDATA_SECTION_NODE
           && rnc.getChildNodes().item(2).getNodeType() == Node.TEXT_NODE, "types TEXT/CDATA/TEXT");

        // ---- ignoringComments strips comments (inner and outer) ----
        DocumentBuilderFactory icf = DocumentBuilderFactory.newInstance();
        icf.setIgnoringComments(true);
        Document dic = parse(icf, "<?xml version=\"1.0\"?><!--outer--><r><!--c--><b/></r>");
        check(dic.getDocumentElement().getChildNodes().getLength() == 1, "inner comment removed");
        check(dic.getChildNodes().getLength() == 1, "outer comment removed from document level");

        // ---- namespaceAware effect on parsed names ----
        DocumentBuilderFactory nf = DocumentBuilderFactory.newInstance();
        Document dna = parse(nf, "<r xmlns:p='urn:p'><p:c/></r>");
        Element elNA = (Element) dna.getElementsByTagName("p:c").item(0);
        check(elNA.getLocalName() == null, "ns-unaware localName null");
        check(elNA.getNodeName().equals("p:c"), "ns-unaware nodeName keeps qname");
        check(elNA.getNamespaceURI() == null && elNA.getPrefix() == null, "ns-unaware ns/prefix null");
        nf.setNamespaceAware(true);
        Document dns = parse(nf, "<r xmlns:p='urn:p'><p:c/></r>");
        Element elNS = (Element) dns.getElementsByTagNameNS("urn:p", "c").item(0);
        check(elNS.getLocalName().equals("c"), "ns-aware localName");
        check(elNS.getNodeName().equals("p:c"), "ns-aware nodeName still qname form");
        check(elNS.getNamespaceURI().equals("urn:p") && elNS.getPrefix().equals("p"), "ns-aware ns/prefix");

        // ---- expandEntityReferences matrix ----
        String docEnt = "<!DOCTYPE r [<!ENTITY e 'expanded'>]><r>&e;</r>";
        DocumentBuilderFactory ef = DocumentBuilderFactory.newInstance();
        ef.setExpandEntityReferences(false);
        Document de = parse(ef, docEnt);
        Node child5 = de.getDocumentElement().getChildNodes().item(0);
        check(child5.getNodeType() == Node.ENTITY_REFERENCE_NODE, "expandER=false keeps ENTITY_REFERENCE node");
        check(child5.getNodeName().equals("e"), "entity reference node name");
        check(de.getDocumentElement().getTextContent().equals(EXPAND_ER_ROOT_TEXT),
              "expandER=false root text (side-specific)");
        Document de2 = parse(DocumentBuilderFactory.newInstance(), docEnt);
        check(de2.getDocumentElement().getChildNodes().item(0).getNodeType() == Node.TEXT_NODE,
              "expandER=true (default) yields TEXT node");

        // ---- validating=true parses a valid DTD document cleanly ----
        DocumentBuilderFactory vf = DocumentBuilderFactory.newInstance();
        vf.setValidating(true);
        Document dv = parse(vf, "<!DOCTYPE r [<!ELEMENT r (#PCDATA)>]><r>ok</r>");
        check(dv.getDocumentElement().getTextContent().equals("ok"), "validating factory parses valid doc");

        // ---- setFeature/getFeature round-trip on SAX feature names ----
        DocumentBuilderFactory ff = DocumentBuilderFactory.newInstance();
        ff.setFeature("http://xml.org/sax/features/namespaces", true);
        check(ff.getFeature("http://xml.org/sax/features/namespaces"), "feature namespaces round-trip true");
        ff.setFeature("http://xml.org/sax/features/namespaces", false);
        check(!ff.getFeature("http://xml.org/sax/features/namespaces"), "feature namespaces round-trip false");
        ff.setFeature("http://xml.org/sax/features/validation", true);
        check(ff.getFeature("http://xml.org/sax/features/validation"), "feature validation round-trip");

        // ---- JAXP attribute: accessExternalDTD value space ----
        DocumentBuilderFactory af = DocumentBuilderFactory.newInstance();
        String aed = javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
        check(aed.equals("http://javax.xml.XMLConstants/property/accessExternalDTD"), "ACCESS_EXTERNAL_DTD constant");
        af.setAttribute(aed, "all");
        check("all".equals(af.getAttribute(aed)), "accessExternalDTD='all' round-trip");
        af.setAttribute(aed, "");
        check("".equals(af.getAttribute(aed)), "accessExternalDTD='' round-trip (deny all)");
        af.setAttribute(aed, null);   // null restores default on Xerces
        check(af.getAttribute(aed) != null, "null value accepted, attribute readable");

        // ---- schema round-trip identity ----
        DocumentBuilderFactory sf = DocumentBuilderFactory.newInstance();
        javax.xml.validation.Schema s = javax.xml.validation.SchemaFactory
                .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new javax.xml.transform.stream.StreamSource(
                        new java.io.StringReader("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
                                + "<xs:element name='r'/></xs:schema>")));
        sf.setSchema(s);
        check(sf.getSchema() == s, "setSchema/getSchema identity");
        sf.setSchema(null);
        check(sf.getSchema() == null, "setSchema(null) clears");

        // ---- builder inherits factory state ----
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        DocumentBuilder b = bf.newDocumentBuilder();
        check(b.isNamespaceAware(), "builder mirrors namespaceAware");
        check(!b.isValidating() && !b.isXIncludeAware(), "builder mirrors defaults");
        DocumentBuilderFactory bf2 = DocumentBuilderFactory.newInstance();
        bf2.setValidating(true);
        check(bf2.newDocumentBuilder().isValidating(), "builder mirrors validating");

        // ---- ignoringElementContentWhitespace flag round-trip (behaviour needs validating) ----
        DocumentBuilderFactory wf = DocumentBuilderFactory.newInstance();
        wf.setIgnoringElementContentWhitespace(true);
        check(wf.isIgnoringElementContentWhitespace(), "ignoringElementContentWhitespace round-trip");
    }
}
