/*
 * @test
 * @summary org.w3c.dom.Entity conformance: reflection shape checks.
 * @run main EntityTest
 */

import java.lang.reflect.*;

public class EntityTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.Entity");
        check(c != null, "org.w3c.dom.Entity loads");
        check(c.getName().equals("org.w3c.dom.Entity"), "binary name is org.w3c.dom.Entity");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.Entity is public");
        check(c.isInterface(), "org.w3c.dom.Entity is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getPublicId"), "declares getPublicId");
        check(declares(c, "getSystemId"), "declares getSystemId");
        check(declares(c, "getNotationName"), "declares getNotationName");
        check(declares(c, "getInputEncoding"), "declares getInputEncoding");
        check(declares(c, "getXmlEncoding"), "declares getXmlEncoding");
        check(declares(c, "getXmlVersion"), "declares getXmlVersion");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Entity contract (parsed DTD declarations) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        String xml = "<!DOCTYPE r [<!ELEMENT r ANY><!ENTITY e1 'v1'>"
            + "<!ENTITY e2 SYSTEM 'e2.xml'>"
            + "<!ENTITY e3 SYSTEM 'e3.bin' NDATA gif>"
            + "<!NOTATION gif PUBLIC 'gif pub' 'gif sys'>]><r>&e1;</r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.DocumentType dt = pd.getDoctype();
        org.w3c.dom.Entity e1 = (org.w3c.dom.Entity) dt.getEntities().getNamedItem("e1");
        org.w3c.dom.Entity e2 = (org.w3c.dom.Entity) dt.getEntities().getNamedItem("e2");
        org.w3c.dom.Entity e3 = (org.w3c.dom.Entity) dt.getEntities().getNamedItem("e3");
        check(org.w3c.dom.Node.ENTITY_NODE == e1.getNodeType(), "entity nodeType 6");
        check("e1".equals(e1.getNodeName()), "entity nodeName is the declaration name");
        check(e1.getParentNode() == null && e1.getOwnerDocument() == pd, "entity parent null, owned by the document");
        check(e1.getNotationName() == null && e1.getPublicId() == null && e1.getSystemId() == null, "internal entity has null external triple");
        check(e1.getChildNodes().getLength() == 0, "Xerces does not expand entity children in the DTD view");
        check("".equals(e1.getTextContent()) || e1.getTextContent() == null, "entity textContent empty/null (not expanded)");
        check(e1.getNodeValue() == null, "entity nodeValue null");
        check(e1.getAttributes() == null && e1.getPrefix() == null, "entity attributes/prefix null");
        check(e2.getSystemId() != null && e2.getSystemId().contains("e2.xml"), "external parsed entity keeps systemId");
        check(e2.getNotationName() == null, "parsed external entity has null notationName");
        check("gif".equals(e3.getNotationName()), "unparsed (NDATA) entity notationName set");
        check(e3.getSystemId() != null, "unparsed entity systemId set");
        // entities are readonly
        try { e1.setNodeValue("x"); check(false, "entity setNodeValue must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entity setNodeValue -> NO_MODIFICATION_ALLOWED_ERR"); }
        try { e1.appendChild(pd.createTextNode("x")); check(false, "entity appendChild must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entity appendChild -> NO_MODIFICATION_ALLOWED_ERR"); }
        // adoption is refused
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        try { other.adoptNode(e1); check(false, "adoptNode(entity) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "adoptNode(entity) -> NO_MODIFICATION_ALLOWED_ERR"); }
        // entity map lookup misses
        check(dt.getEntities().getNamedItem("nope") == null, "entities getNamedItem(unknown) null");
        // factory-created doctype carries no entities
        org.w3c.dom.Document blank = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DocumentType cdt = blank.getImplementation().createDocumentType("q", null, null);
        check(cdt.getEntities().getLength() == 0, "factory doctype entities empty");
    }
}
