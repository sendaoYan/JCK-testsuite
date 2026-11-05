/*
 * @test
 * @summary org.w3c.dom.EntityReference conformance: reflection shape checks.
 * @run main EntityReferenceTest
 */

import java.lang.reflect.*;

public class EntityReferenceTest {
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
        Class<?> c = Class.forName("org.w3c.dom.EntityReference");
        check(c != null, "org.w3c.dom.EntityReference loads");
        check(c.getName().equals("org.w3c.dom.EntityReference"), "binary name is org.w3c.dom.EntityReference");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.EntityReference is public");
        check(c.isInterface(), "org.w3c.dom.EntityReference is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral EntityReference contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        String xml = "<!DOCTYPE r [<!ENTITY e1 'v1'>]><r>&e1;</r>";
        // expansion ON (default): reference replaced by its replacement text
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        check(pd.getDocumentElement().getChildNodes().getLength() == 1
            && org.w3c.dom.Node.TEXT_NODE == pd.getDocumentElement().getFirstChild().getNodeType(), "default parsing expands entity references to text");
        check("v1".equals(pd.getDocumentElement().getTextContent()), "expanded entity contributes replacement text");
        // expansion OFF: EntityReference node appears
        javax.xml.parsers.DocumentBuilderFactory fx = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        fx.setExpandEntityReferences(false);
        fx.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        org.w3c.dom.Document pdx = fx.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Node erNode = pdx.getDocumentElement().getFirstChild();
        check(org.w3c.dom.Node.ENTITY_REFERENCE_NODE == erNode.getNodeType(), "expandEntityReferences(false) keeps EntityReference nodes");
        check("e1".equals(erNode.getNodeName()), "parsed EntityReference nodeName is the entity name");
        check(erNode.getChildNodes().getLength() == 0, "Xerces leaves parsed EntityReference children unexpanded");
        // manual factory
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.EntityReference er = d.createEntityReference("e9");
        check(org.w3c.dom.Node.ENTITY_REFERENCE_NODE == er.getNodeType(), "manual entityReference nodeType 5");
        check("e9".equals(er.getNodeName()) && er.getOwnerDocument() == d, "manual entityReference name/owner");
        check(er.getChildNodes().getLength() == 0, "manual entityReference has no children");
        check(er.getParentNode() == null, "factory entityReference unparented");
        check(er.getNodeValue() == null, "entityReference nodeValue null");
        // name legality
        try { d.createEntityReference("a b"); check(false, "createEntityReference(illegal) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "createEntityReference('a b') -> INVALID_CHARACTER_ERR"); }
        try { d.createEntityReference(null); check(false, "createEntityReference(null) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "createEntityReference(null) -> INVALID_CHARACTER_ERR"); }
        check(d.createEntityReference("undeclared") != null, "Xerces does not validate the name against declared entities");
        // readonly: children cannot be attached
        try { er.appendChild(d.createElement("x")); check(false, "entityReference appendChild must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entityReference appendChild -> NO_MODIFICATION_ALLOWED_ERR"); }
        // usable as a normal tree member
        org.w3c.dom.Element host = d.createElement("host");
        d.appendChild(host);
        host.appendChild(er);
        check(er.getParentNode() == host && host.getFirstChild() == er, "entityReference can be placed in content");
        // clone/import keep the reference
        check("e9".equals(((org.w3c.dom.EntityReference) er.cloneNode(true)).getNodeName()), "entityReference clone keeps name");
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        check("e9".equals(((org.w3c.dom.EntityReference) other.importNode(er, true)).getNodeName()), "entityReference import keeps name");
        // textContent of unexpanded reference is empty
        check("".equals(host.getTextContent()) || host.getTextContent() == null, "unexpanded entityReference contributes no text");
    }
}
