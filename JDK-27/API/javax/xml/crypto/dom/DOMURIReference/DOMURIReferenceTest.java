/*
 * @test
 * @summary javax.xml.crypto.dom.DOMURIReference conformance.
 * @run main DOMURIReferenceTest
 */

import javax.xml.crypto.URIReference;
import javax.xml.crypto.dom.DOMURIReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;

public class DOMURIReferenceTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dom.DOMURIReference");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.URIReference"), "extends URIReference");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getHere"), "adds getHere");

        // custom implementation exercising the full contract
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final Element here = d.createElement("Reference");
        here.setAttribute("URI", "#x");
        DOMURIReference ref = new DOMURIReference() {
            public String getURI() { return here.getAttribute("URI"); }
            public String getType() { return "http://www.w3.org/2000/09/xmldsig#Object"; }
            public Node getHere() { return here; }
        };
        check(ref instanceof URIReference, "is a URIReference");
        check("#x".equals(ref.getURI()), "getURI round-trip");
        check("http://www.w3.org/2000/09/xmldsig#Object".equals(ref.getType()), "getType round-trip");
        check(ref.getHere() == here, "getHere identity");
        check(ref.getHere().getNodeType() == Node.ELEMENT_NODE, "here node is the attribute-carrying element");

        // Reference and RetrievalMethod results obtained from a real signature are DOMURIReferences
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.Reference r = sf.newReference("#x",
            sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null));
        check(r instanceof URIReference, "Reference is a URIReference");
        Document sd = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = sd.createElement("r");
        sd.appendChild(root);
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256", null),
            java.util.Collections.singletonList(r));
        javax.crypto.SecretKey k = new javax.crypto.spec.SecretKeySpec(new byte[32], "HmacSHA256");
        sf.newXMLSignature(si, null,
            java.util.Collections.singletonList(sf.newXMLObject(null, "x", null, null)), null, null)
            .sign(new javax.xml.crypto.dsig.dom.DOMSignContext(k, root));
        Element sigEl = (Element) root.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature").item(0);
        javax.xml.crypto.dsig.dom.DOMValidateContext vc =
            new javax.xml.crypto.dsig.dom.DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(k), sigEl);
        javax.xml.crypto.dsig.Reference vr = (javax.xml.crypto.dsig.Reference)
            sf.unmarshalXMLSignature(vc).getSignedInfo().getReferences().get(0);
        check(vr instanceof DOMURIReference, "unmarshalled Reference implements DOMURIReference");
        Node hereNode = ((DOMURIReference) vr).getHere();
        check(hereNode.getNodeType() == Node.ATTRIBUTE_NODE, "getHere returns the URI attribute node");
        check("URI".equals(hereNode.getNodeName()), "here attribute name is URI");
        check("#x".equals(hereNode.getNodeValue()), "here attribute value matches the reference URI");
        check("Reference".equals(((org.w3c.dom.Attr) hereNode).getOwnerElement().getLocalName()),
            "here attribute is owned by the Reference element");
        // RetrievalMethod from KeyInfoFactory unmarshals to DOMURIReference too
        javax.xml.crypto.dsig.keyinfo.RetrievalMethod rm =
            sf.getKeyInfoFactory().newRetrievalMethod("#kk", null, null);
        check(rm instanceof URIReference, "RetrievalMethod is a URIReference");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
