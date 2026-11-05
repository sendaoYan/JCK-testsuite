/*
 * @test
 * @summary org.w3c.dom.DocumentType conformance: reflection shape checks.
 * @run main DocumentTypeTest
 */

import java.lang.reflect.*;

public class DocumentTypeTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DocumentType");
        check(c != null, "org.w3c.dom.DocumentType loads");
        check(c.getName().equals("org.w3c.dom.DocumentType"), "binary name is org.w3c.dom.DocumentType");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DocumentType is public");
        check(c.isInterface(), "org.w3c.dom.DocumentType is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "getEntities"), "declares getEntities");
        check(declares(c, "getNotations"), "declares getNotations");
        check(declares(c, "getPublicId"), "declares getPublicId");
        check(declares(c, "getSystemId"), "declares getSystemId");
        check(declares(c, "getInternalSubset"), "declares getInternalSubset");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DocumentType contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DOMImplementation impl = d.getImplementation();
        // factory side
        org.w3c.dom.DocumentType cdt = impl.createDocumentType("q", "pub", "sys");
        check(org.w3c.dom.Node.DOCUMENT_TYPE_NODE == cdt.getNodeType(), "doctype nodeType 10");
        check("q".equals(cdt.getNodeName()) && "q".equals(cdt.getName()), "doctype nodeName == name");
        check("pub".equals(cdt.getPublicId()) && "sys".equals(cdt.getSystemId()), "createDocumentType publicId/systemId round-trip");
        check(cdt.getInternalSubset() == null, "factory doctype has no internal subset");
        check(cdt.getEntities().getLength() == 0 && cdt.getNotations().getLength() == 0, "factory doctype has empty entities/notations");
        check(cdt.getOwnerDocument() == null || cdt.getOwnerDocument() == d, "factory doctype ownerless until adopted");
        try { impl.createDocumentType("a b", null, null); check(false, "createDocumentType(illegal) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "createDocumentType('a b') -> INVALID_CHARACTER_ERR"); }
        try { impl.createDocumentType("", null, null); check(false, "createDocumentType('') must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NAMESPACE_ERR, "createDocumentType('') -> NAMESPACE_ERR (qualified-name rules)"); }
        boolean npe = false;
        try { impl.createDocumentType(null, null, null); } catch (NullPointerException expected) { npe = true; }
        check(npe, "createDocumentType(null) -> NullPointerException");
        // parsed side with public/system ids and internal subset
        String xml = "<!DOCTYPE r PUBLIC 'pub' 'sys' [<!ENTITY e1 'v1'>"
            + "<!ENTITY e2 SYSTEM 'e2.xml'>"
            + "<!ENTITY e3 SYSTEM 'e3.bin' NDATA gif>"
            + "<!NOTATION gif PUBLIC 'gp' 'gs'>]><r/>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.DocumentType dt = pd.getDoctype();
        check(dt != null && dt.getOwnerDocument() == pd, "parsed doctype owned by the document");
        check("r".equals(dt.getName()), "parsed doctype name is the document element name");
        check("pub".equals(dt.getPublicId()) && "sys".equals(dt.getSystemId()), "parsed doctype records external ids");
        check(dt.getInternalSubset() != null && dt.getInternalSubset().contains("<!ENTITY e1 'v1'>"), "internal subset preserves declarations verbatim");
        check(dt.getNodeValue() == null && dt.getTextContent() == null, "doctype nodeValue/textContent null");
        // readonly structure
        dt.setNodeValue("x");
        check(dt.getNodeValue() == null, "doctype setNodeValue is a silent no-op");
        try { dt.appendChild(pd.createElement("z")); check(false, "doctype appendChild must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "doctype appendChild -> NO_MODIFICATION_ALLOWED_ERR"); }
        // entities/notations maps are readonly views
        org.w3c.dom.NamedNodeMap ents = dt.getEntities();
        check(ents.getLength() == 3, "three declared entities");
        try { ents.removeNamedItem("e1"); check(false, "entities removeNamedItem must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entities map remove -> NO_MODIFICATION_ALLOWED_ERR"); }
        try { ents.setNamedItem(pd.createAttribute("x")); check(false, "entities setNamedItem must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entities map set -> NO_MODIFICATION_ALLOWED_ERR"); }
        check(ents.getNamedItem("zz") == null, "entities getNamedItem(missing) null");
        org.w3c.dom.NamedNodeMap nots = dt.getNotations();
        check(nots.getLength() == 1 && nots.item(99) == null, "notations map length and out-of-bounds null");
        try { nots.removeNamedItem("gif"); check(false, "notations removeNamedItem must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "notations map remove -> NO_MODIFICATION_ALLOWED_ERR"); }
        // cross-document operations are unsupported
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        try { other.adoptNode(dt); check(false, "adoptNode(doctype) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "adoptNode(doctype) -> NOT_SUPPORTED_ERR"); }
        try { other.importNode(dt, true); check(false, "importNode(doctype) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "importNode(doctype) -> NOT_SUPPORTED_ERR"); }
        // createDocument cross-document doctype rule
        try { impl.createDocument(null, "z", dt); check(false, "createDocument(owned doctype) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR, "createDocument(doctype owned elsewhere) -> WRONG_DOCUMENT_ERR"); }
        // createDocument happy path installs both doctype and root
        org.w3c.dom.DocumentType fresh = impl.createDocumentType("q", null, null);
        org.w3c.dom.Document made = impl.createDocument("urn:n", "n:doc", fresh);
        check(made.getChildNodes().getLength() == 2 && made.getDoctype() == fresh, "createDocument installs doctype as first child");
        check("urn:n".equals(made.getDocumentElement().getNamespaceURI()), "createDocument root namespace");
    }
}
