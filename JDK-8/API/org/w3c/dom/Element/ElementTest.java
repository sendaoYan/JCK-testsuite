/*
 * @test
 * @summary org.w3c.dom.Element conformance: attribute CRUD, namespace attributes, attr-node channels, getElementsByTagName(NS), hasAttribute.
 * @run main ElementTest
 */

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

public class ElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean domCode(Thrower t, short code) {
        try { t.run(); return false; }
        catch (DOMException e) { return e.code == code; }
        catch (Exception e) { return false; }
    }

    static Document newDoc() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().newDocument();
    }

    public static void main(String[] args) throws Exception {
        Document d = newDoc();
        Element e = d.createElement("person");

        // ---- tag name ----
        check("person".equals(e.getTagName()), "getTagName");

        // ---- attribute CRUD (null/invalid name checked on fresh element before any real attribute) ----
        check(domCode(() -> e.setAttribute(null, "v"), DOMException.INVALID_CHARACTER_ERR), "setAttribute(null name) -> INVALID_CHARACTER_ERR");
        check(domCode(() -> e.setAttribute("1bad", "v"), DOMException.INVALID_CHARACTER_ERR), "setAttribute(name starting digit) -> INVALID_CHARACTER_ERR");
        check("".equals(e.getAttribute("missing")), "getAttribute(missing) returns empty string");
        e.setAttribute("id", "1");
        check("1".equals(e.getAttribute("id")), "setAttribute/getAttribute");
        check(e.hasAttribute("id"), "hasAttribute true");
        check(!e.hasAttribute("missing"), "hasAttribute false");
        e.setAttribute("id", "2");
        check("2".equals(e.getAttribute("id")), "setAttribute overwrites");
        e.removeAttribute("id");
        check(!e.hasAttribute("id"), "removeAttribute removes");
        check("".equals(e.getAttribute("id")), "removed attribute reads empty");
        // removeAttribute(null) on JDK impl -> NPE (deterministic while an attribute is present)
        e.setAttribute("tmp", "1");
        boolean npe = false;
        try { e.removeAttribute(null); } catch (NullPointerException ex) { npe = true; }
        check(npe, "removeAttribute(null) -> NPE");
        e.removeAttribute("tmp");

        // ---- attribute node channels ----
        Attr a = d.createAttribute("lang");
        a.setValue("en");
        Attr oldAttr = e.setAttributeNode(a);
        check(oldAttr == null, "setAttributeNode returns null for new attr");
        check("en".equals(e.getAttribute("lang")), "setAttributeNode reflected in getAttribute");
        check(e.getAttributeNode("lang") == a, "getAttributeNode returns same node");
        check(a.getOwnerElement() == e, "attribute ownerElement");
        Attr a2 = d.createAttribute("lang");
        a2.setValue("fr");
        Attr replaced = e.setAttributeNode(a2);
        check(replaced == a, "setAttributeNode returns previous attr");
        check("fr".equals(e.getAttribute("lang")), "setAttributeNode replaces value");
        Attr removed = e.removeAttributeNode(a2);
        check(removed == a2, "removeAttributeNode returns removed node");
        check(removed.getOwnerElement() == null, "removeAttributeNode clears ownerElement");
        check(domCode(() -> e.removeAttributeNode(a2), DOMException.NOT_FOUND_ERR), "removeAttributeNode(absent) -> NOT_FOUND_ERR");

        // ---- namespace attributes ----
        e.setAttributeNS("urn:meta", "m:kind", "x");
        check("x".equals(e.getAttributeNS("urn:meta", "kind")), "setAttributeNS/getAttributeNS");
        check(e.hasAttributeNS("urn:meta", "kind"), "hasAttributeNS true");
        check(!e.hasAttributeNS("urn:meta", "other"), "hasAttributeNS false");
        e.setAttributeNS("urn:meta", "m:kind", "y");
        check("y".equals(e.getAttributeNS("urn:meta", "kind")), "setAttributeNS overwrites");
        check(e.getAttributeNodeNS("urn:meta", "kind") != null, "getAttributeNodeNS");
        e.removeAttributeNS("urn:meta", "kind");
        check(!e.hasAttributeNS("urn:meta", "kind"), "removeAttributeNS removes");
        check("".equals(e.getAttributeNS("urn:meta", "kind")), "removed NS attribute reads empty");
        check(domCode(() -> e.setAttributeNS(null, "m:kind", "v"), DOMException.NAMESPACE_ERR), "setAttributeNS(null uri, prefixed) -> NAMESPACE_ERR");

        // ---- getElementsByTagName(NS) ----
        d.appendChild(e);
        e.appendChild(d.createElement("name"));
        e.appendChild(d.createElement("name"));
        NodeList names = e.getElementsByTagName("name");
        check(names.getLength() == 2, "getElementsByTagName count");
        Element nsName = d.createElementNS("urn:meta", "m:name");
        e.appendChild(nsName);
        check(e.getElementsByTagNameNS("urn:meta", "name").getLength() == 1, "getElementsByTagNameNS count");
        check(e.getElementsByTagNameNS("*", "*").getLength() == 3, "getElementsByTagNameNS */* matches all descendant elements");

        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: element attribute asymmetry / ns family / scan / TypeInfo ----
    static void deepContract() throws Exception {
        Document d = newDoc();

        // missing-attribute asymmetry: "" vs null
        Element e = d.createElement("e");
        check(e.getAttribute("missing").isEmpty(), "getAttribute(missing) empty string");
        check(e.getAttributeNode("missing") == null, "getAttributeNode(missing) null");
        check(!e.hasAttribute("missing"), "hasAttribute(missing) false");
        check(e.getAttributeNS("urn:x", "missing").isEmpty(), "getAttributeNS(missing) empty string");
        check(!e.hasAttributeNS("urn:x", "missing"), "hasAttributeNS(missing) false");
        // removeAttribute(missing) silent vs removeAttributeNode(missing) throws
        e.removeAttribute("missing");
        check(true, "removeAttribute(missing) silent");
        check(domCode(() -> e.removeAttributeNode(d.createAttribute("nope")), DOMException.NOT_FOUND_ERR), "removeAttributeNode(unmounted) -> NOT_FOUND_ERR");
        // duplicate setAttribute overwrites, count stable
        e.setAttribute("k", "v1");
        e.setAttribute("k", "v2");
        check("v2".equals(e.getAttribute("k")), "duplicate setAttribute overwrites");
        check(e.getAttributes().getLength() == 1, "duplicate setAttribute keeps count");
        // value stringification (setAttribute only accepts String; setAttributeNode numeric text)
        e.setAttribute("num", String.valueOf(7));
        check("7".equals(e.getAttribute("num")), "value stored as string");
        // setAttributeNode replacement semantics
        Attr a1 = d.createAttribute("dup");
        a1.setValue("first");
        e.setAttributeNode(a1);
        Attr a2 = d.createAttribute("dup");
        a2.setValue("second");
        Attr old = e.setAttributeNode(a2);
        check(old == a1, "setAttributeNode(same name) returns previous attr node");
        check("second".equals(e.getAttribute("dup")), "setAttributeNode replacement visible");
        check(old.getOwnerElement() == null, "replaced attr ownerElement cleared");
        check(a2.getOwnerElement() == e, "new attr ownerElement set");
        // illegal names
        check(domCode(() -> e.setAttribute("a b", "v"), DOMException.INVALID_CHARACTER_ERR), "setAttribute('a b') -> INVALID_CHARACTER_ERR");
        check(domCode(() -> e.setAttribute("", "v"), DOMException.INVALID_CHARACTER_ERR), "setAttribute('') -> INVALID_CHARACTER_ERR");
        check(domCode(() -> d.createElement("bad name"), DOMException.INVALID_CHARACTER_ERR), "createElement('bad name') -> INVALID_CHARACTER_ERR");
        // removeAttributeNode lifecycle
        Attr victim = e.getAttributeNode("k");
        Attr returned = e.removeAttributeNode(victim);
        check(returned == victim && victim.getOwnerElement() == null, "removeAttributeNode returns detached attr");
        check(domCode(() -> e.removeAttributeNode(victim), DOMException.NOT_FOUND_ERR), "removeAttributeNode twice -> NOT_FOUND_ERR");
        // ns attribute family
        Element ne = d.createElementNS("urn:x", "p:e");
        ne.setAttributeNS("urn:y", "q:a", "1");
        ne.setAttribute("plain", "2");
        check("1".equals(ne.getAttributeNS("urn:y", "a")), "getAttributeNS round-trip");
        check(ne.hasAttributeNS("urn:y", "a"), "hasAttributeNS true");
        check(ne.getAttributeNodeNS("urn:y", "a") != null, "getAttributeNodeNS non-null");
        check(ne.getAttributes().getLength() == 2, "ns + plain attr counted once each");
        ne.removeAttributeNS("urn:y", "a");
        check(!ne.hasAttributeNS("urn:y", "a"), "removeAttributeNS removes");
        ne.removeAttributeNS("urn:none", "zz");
        check(true, "removeAttributeNS(missing) silent");
        Attr nsA = d.createAttributeNS("urn:z", "w:b");
        ne.setAttributeNodeNS(nsA);
        check(ne.getAttributes().getNamedItemNS("urn:z", "b") == nsA, "setAttributeNodeNS / getNamedItemNS round-trip");
        // namespaced tag scan
        ne.appendChild(d.createElementNS("urn:x", "p:child"));
        ne.appendChild(d.createElement("child"));
        check(ne.getElementsByTagNameNS("urn:x", "child").getLength() == 1, "ns scan exact");
        check(ne.getElementsByTagNameNS("*", "child").getLength() == 1, "ns wildcard-local only matches ns nodes (Xerces)");
        check(ne.getElementsByTagNameNS("*", "*").getLength() == 2, "ns full wildcard");
        // schema type info reachable (null type name without grammar)
        TypeInfo ti = ne.getSchemaTypeInfo();
        check(ti != null, "getSchemaTypeInfo non-null");
        check(ti.getTypeName() == null, "typeName null without schema");
        check(ti.getTypeNamespace() == null, "typeNamespace null without schema");
        check(!ti.isDerivedFrom("http://www.w3.org/2001/XMLSchema", "string", TypeInfo.DERIVATION_RESTRICTION), "isDerivedFrom false without schema");
        // text content
        ne.setTextContent("hello");
        check(ne.getChildNodes().getLength() == 1 && ne.getFirstChild().getNodeType() == Node.TEXT_NODE, "setTextContent leaves single text child");
        ne.appendChild(d.createElement("sub"));
        ne.getFirstChild().setNodeValue("AB");
        check("AB".equals(ne.getTextContent()), "getTextContent concatenates descendants");
        // equals across same-shape elements
        Element eq1 = d.createElement("eq");
        eq1.setAttribute("x", "1");
        Element eq2 = d.createElement("eq");
        eq2.setAttribute("x", "1");
        check(eq1.isEqualNode(eq2), "same-shape elements isEqualNode");
        eq2.setAttribute("x", "2");
        check(!eq1.isEqualNode(eq2), "attr value difference breaks equality");
        // cloneNode deep keeps attributes and children
        Element host = d.createElement("host");
        host.setAttribute("h", "1");
        host.appendChild(d.createElement("inner"));
        Element hostClone = (Element) host.cloneNode(true);
        check("1".equals(hostClone.getAttribute("h")), "clone keeps attributes");
        check(hostClone.getElementsByTagName("inner").getLength() == 1, "deep clone keeps children");
        // tagName / nodeName identity
        check(ne.getTagName().equals(ne.getNodeName()), "getTagName == getNodeName");
        Element qnamed = d.createElementNS("urn:x", "p:tag");
        check("p:tag".equals(qnamed.getTagName()) && "tag".equals(qnamed.getLocalName()) && "p".equals(qnamed.getPrefix()), "qualified name decomposition");
    }

}
