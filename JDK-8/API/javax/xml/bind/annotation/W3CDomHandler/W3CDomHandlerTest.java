/*
 * @test
 * @summary javax.xml.bind.annotation.W3CDomHandler conformance: W3C DOM handler createUnmarshaller/getElement/marshal contract.
 * @run main W3CDomHandlerTest
 */

import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.DomHandler;
import javax.xml.bind.annotation.W3CDomHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class W3CDomHandlerTest {
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
        javax.xml.bind.annotation.W3CDomHandler h = new javax.xml.bind.annotation.W3CDomHandler();
        check(h.getBuilder() == null, "default builder null");
        javax.xml.transform.dom.DOMResult r = h.createUnmarshaller(null);
        check(r != null, "createUnmarshaller non-null");
        testClassContract();
        testCreateUnmarshaller();
        testGetElement();
        testMarshal();
        testBuilderLifecycle();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() {
        Class<W3CDomHandler> c = W3CDomHandler.class;
        check("javax.xml.bind.annotation.W3CDomHandler".equals(c.getName()), "FQCN exact");
        check("W3CDomHandler".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(DomHandler.class.isAssignableFrom(c), "DomHandler.isAssignableFrom(W3CDomHandler)");
    }

    static void testClassContract() {
        Class<W3CDomHandler> c = W3CDomHandler.class;
        check(!Modifier.isFinal(c.getModifiers()), "class not final (subclassable)");
        check(Modifier.isPublic(c.getModifiers()), "class public");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == DomHandler.class,
                "implements DomHandler<Element, DOMResult> (erased)");
        check(c.getConstructors().length == 2, "two public constructors: no-arg and DocumentBuilder");
        DomHandler<Element, DOMResult> asIface = new W3CDomHandler();
        check(asIface != null, "assignable to DomHandler interface reference");
        Document doc = newDocument();
        Element el = doc.createElement("probe");
        doc.appendChild(el);
        check(asIface.getElement(new DOMResult(doc)) == el, "interface-typed getElement works");
        check(asIface.marshal(el, null) instanceof DOMSource, "interface-typed marshal works");
        check(asIface.createUnmarshaller(null) != null, "interface-typed createUnmarshaller works");
    }

    static void testCreateUnmarshaller() {
        W3CDomHandler h = new W3CDomHandler();
        DOMResult r1 = h.createUnmarshaller(null);
        check(r1 != null, "createUnmarshaller(null) returns DOMResult");
        check(r1.getNode() == null, "fresh DOMResult has no node yet (unmarshaller fills it)");
        check(r1.getSystemId() == null, "fresh DOMResult systemId null");
        DOMResult r2 = h.createUnmarshaller(null);
        check(r1 != r2, "each call returns a distinct DOMResult");
        check(r1.getClass() == DOMResult.class, "result type is exactly DOMResult");
    }

    static void testGetElement() {
        W3CDomHandler h = new W3CDomHandler();
        Document doc = newDocument();
        Element root = doc.createElementNS("urn:x", "root");
        doc.appendChild(root);
        Element resolved = h.getElement(new DOMResult(doc));
        check(resolved == root, "getElement(DOMResult over Document) returns the document element");
        Element standalone = doc.createElement("alone");
        check(h.getElement(new DOMResult(standalone)) == standalone,
                "getElement(DOMResult over Element) returns the element itself");
        try {
            h.getElement(new DOMResult());
            check(false, "getElement on node-less DOMResult should throw");
        } catch (NullPointerException e) {
            check(true, "getElement on node-less DOMResult throws NPE");
        }
    }

    static void testMarshal() {
        W3CDomHandler h = new W3CDomHandler();
        Document doc = newDocument();
        Element root = doc.createElementNS("urn:x", "payload");
        root.setAttribute("id", "7");
        doc.appendChild(root);
        Source s = h.marshal(root, null);
        check(s != null, "marshal returns non-null Source");
        check(s instanceof DOMSource, "marshal returns DOMSource");
        check(((DOMSource) s).getNode() == root, "marshal wraps the very same element instance (no copy)");
        check(s.getSystemId() == null, "marshal Source systemId null");
        Source s2 = h.marshal(root, null);
        check(s != s2, "each marshal returns a fresh Source");
        check(((DOMSource) s2).getNode() == root, "second marshal still wraps same element");
        Element other = doc.createElement("other");
        check(((DOMSource) h.marshal(other, null)).getNode() == other, "marshal of a different element");
        Document doc2 = newDocument();
        Element foreign = doc2.createElement("foreign");
        check(((DOMSource) h.marshal(foreign, null)).getNode() == foreign, "marshal of element from another document");
    }

    static void testBuilderLifecycle() throws Exception {
        W3CDomHandler h = new W3CDomHandler();
        check(h.getBuilder() == null, "no-arg ctor leaves builder null");
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        h.setBuilder(db);
        check(h.getBuilder() == db, "setBuilder/getBuilder round-trip identity");
        W3CDomHandler h2 = new W3CDomHandler(db);
        check(h2.getBuilder() == db, "DocumentBuilder ctor stores builder");
        DocumentBuilder db2 = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        h2.setBuilder(db2);
        check(h2.getBuilder() == db2, "setBuilder replaces previous builder");
        check(h2.createUnmarshaller(null) != null, "createUnmarshaller works with builder installed");
    }

    static Document newDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
