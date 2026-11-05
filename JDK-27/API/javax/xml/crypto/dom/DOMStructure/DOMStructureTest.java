/*
 * @test
 * @summary javax.xml.crypto.dom.DOMStructure conformance.
 * @run main DOMStructureTest
 */

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;

public class DOMStructureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dom.DOMStructure");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(javax.xml.crypto.XMLStructure.class.isAssignableFrom(c), "implements XMLStructure");
        check(javax.xml.crypto.dsig.keyinfo.KeyInfo.class.isAssignableFrom(c) == false, "distinct from KeyInfo");
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element e = d.createElement("payload");
        e.setAttribute("k", "v");
        e.appendChild(d.createTextNode("text"));
        // wrap and read back by identity
        DOMStructure s = new DOMStructure(e);
        check(s.getNode() == e, "getNode identity round-trip");
        check(s.getNode().getNodeType() == Node.ELEMENT_NODE, "element node type");
        // wrap any node kind: document, text, attribute, comment
        check(new DOMStructure(d).getNode().getNodeType() == Node.DOCUMENT_NODE, "document node wrappable");
        check(new DOMStructure(d.createTextNode("t")).getNode().getNodeType() == Node.TEXT_NODE, "text node wrappable");
        check(new DOMStructure(d.createComment("c")).getNode().getNodeType() == Node.COMMENT_NODE, "comment node wrappable");
        // null rejected
        try { DOMStructure t = new DOMStructure(null); check(false, "null node should throw: " + t); }
        catch (NullPointerException npe) { check("node cannot be null".equals(npe.getMessage()), "null node message"); }
        // feature contract
        try { s.isFeatureSupported(null); check(false, "null feature NPE"); }
        catch (NullPointerException npe) { check(true, "isFeatureSupported(null) NPE"); }
        check(!s.isFeatureSupported("anything"), "unknown feature false");
        // usable as XMLObject content inside a real signature
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.XMLObject xo = sf.newXMLObject(java.util.Collections.singletonList(s), "dom-obj", null, null);
        check(xo.getContent().get(0) == s, "DOMStructure accepted as XMLObject content");
        Document sd = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = sd.createElement("r");
        sd.appendChild(root);
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256", null),
            java.util.Collections.singletonList(sf.newReference("#dom-obj",
                sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null))));
        javax.crypto.SecretKey k = new javax.crypto.spec.SecretKeySpec(new byte[32], "HmacSHA256");
        sf.newXMLSignature(si, null, java.util.Collections.singletonList(xo), null, null)
            .sign(new javax.xml.crypto.dsig.dom.DOMSignContext(k, root));
        Element marshalled = (Element) root.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Object").item(0);
        check(marshalled != null && marshalled.getAttribute("Id").equals("dom-obj"), "wrapped subtree marshalled inside ds:Object");
        check(marshalled.getElementsByTagName("payload").getLength() == 1, "payload element serialized into the Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
