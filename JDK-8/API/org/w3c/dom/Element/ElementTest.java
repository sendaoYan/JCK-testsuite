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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
