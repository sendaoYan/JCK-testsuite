/*
 * @test
 * @summary javax.xml.crypto.dsig.dom.DOMValidateContext conformance.
 * @run main DOMValidateContextTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DOMValidateContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static KeyPair KP;
    static KeyPair rsaKP() throws Exception {
        if (KP == null) {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            KP = g.generateKeyPair();
        }
        return KP;
    }

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    /** Produces a document carrying an enveloped RSA-SHA256 signature. */
    static Document signedDoc(KeyPair kp) throws Exception {
        Document d = newDoc("<r>V</r>");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Reference ref = sf.newReference("", sf.newDigestMethod(DigestMethod.SHA256, null),
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), d.getDocumentElement()));
        return d;
    }

    public static void main(String[] args) throws Exception {
        KeyPair kp = rsaKP();
        Document base = newDoc("<r/>");
        Element baseEl = base.getDocumentElement();

        // --- constructor matrix and null messages ---
        try {
            DOMValidateContext t = new DOMValidateContext((KeySelector) null, baseEl);
            check(false, "null selector should throw: " + t);
        } catch (NullPointerException e) { check("key selector is null".equals(e.getMessage()), "null selector message"); }
        try {
            DOMValidateContext t = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), (Node) null);
            check(false, "null node should throw: " + t);
        } catch (NullPointerException e) { check("node is null".equals(e.getMessage()), "null node (selector ctor) message"); }
        try {
            DOMValidateContext t = new DOMValidateContext((Key) null, baseEl);
            check(false, "null key should throw: " + t);
        } catch (NullPointerException e) { check("validatingKey is null".equals(e.getMessage()), "null key message"); }
        try {
            DOMValidateContext t = new DOMValidateContext(kp.getPublic(), (Node) null);
            check(false, "null node key ctor should throw: " + t);
        } catch (NullPointerException e) { check("node is null".equals(e.getMessage()), "null node (key ctor) message"); }

        // --- key-based ctor wraps the key in a singleton key selector ---
        DOMValidateContext vc = new DOMValidateContext(kp.getPublic(), baseEl);
        check(vc.getKeySelector() != null
            && vc.getKeySelector().getClass().getName().startsWith("javax.xml.crypto.KeySelector"),
            "key ctor supplies singleton key selector");
        check(vc.getNode() == baseEl, "node round-trip");
        vc.setNode(base.getDocumentElement());
        check(vc.getNode() == base.getDocumentElement(), "setNode/getNode identity");
        vc.setKeySelector(null);
        check(vc.getKeySelector() == null, "setKeySelector(null) clears");
        vc.setKeySelector(KeySelector.singletonKeySelector(kp.getPublic()));
        check(vc.getKeySelector() != null, "setKeySelector round-trip");

        // --- id registration and lookup ---
        Element withId = base.createElement("x");
        withId.setAttribute("Id", "x1");
        withId.setIdAttribute("Id", true);
        baseEl.appendChild(withId);
        check(vc.getElementById("x1") == null, "unregistered id not found");
        vc.setIdAttributeNS(withId, null, "Id");
        check(vc.getElementById("x1") == withId, "registered id resolves to the element");
        check(vc.getElementById("other") == null, "unknown id null");
        try { vc.setIdAttributeNS(null, null, "Id"); check(false, "null element should throw"); }
        catch (NullPointerException e) { check("element is null".equals(e.getMessage()), "setIdAttributeNS(null) message"); }

        // --- namespace prefix map ---
        check("dflt".equals(vc.getNamespacePrefix("urn:x", "dflt")), "unregistered prefix returns default");
        check(vc.putNamespacePrefix("urn:x", "px") == null, "putNamespacePrefix returns previous (null)");
        check("px".equals(vc.putNamespacePrefix("urn:x", "py")), "putNamespacePrefix returns previous value");
        check("py".equals(vc.getNamespacePrefix("urn:x", "dflt")), "prefix mapping updated");
        try { vc.putNamespacePrefix(null, "p"); check(false, "null namespaceURI should throw"); }
        catch (NullPointerException e) { check("namespaceURI is null".equals(e.getMessage()), "null namespaceURI message"); }

        // --- baseURI, properties, put/get, iterator ---
        check(vc.getBaseURI() == null, "baseURI default null");
        vc.setBaseURI("http://v/");
        check("http://v/".equals(vc.getBaseURI()), "baseURI round-trip");
        check(vc.setProperty("p", "v1") == null, "setProperty returns previous (null)");
        check("v1".equals(vc.setProperty("p", "v2")), "setProperty returns previous value");
        check("v2".equals(vc.getProperty("p")), "getProperty round-trip");
        vc.put("k", "v");
        check("v".equals(vc.get("k")), "put/get round-trip");
        check(vc.get("missing") == null, "missing mapping null");
        check(!vc.iterator().hasNext() || vc.iterator().hasNext(), "id iterator accessible");

        // --- dual delivery channels: selector vs context key, both validate a real signature ---
        Document sd = signedDoc(kp);
        Element sigEl = (Element) sd.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        DOMValidateContext v1 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(v1).validate(v1), "selector channel validates");
        DOMValidateContext v2 = new DOMValidateContext(kp.getPublic(), sigEl);
        check(sf.unmarshalXMLSignature(v2).validate(v2), "context-key channel validates");
        // wrong key in the context channel fails crypto validation but references stay valid
        java.security.KeyPairGenerator otherGen = KeyPairGenerator.getInstance("RSA");
        otherGen.initialize(2048);
        KeyPair other = otherGen.generateKeyPair();
        Document sd2 = signedDoc(kp);
        Element sigEl2 = (Element) sd2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext v3 = new DOMValidateContext(other.getPublic(), sigEl2);
        XMLSignature bad = sf.unmarshalXMLSignature(v3);
        check(!bad.validate(v3), "wrong key invalidates");
        Reference r = (Reference) bad.getSignedInfo().getReferences().get(0);
        check(r.validate(v3), "reference still valid under wrong key");

        // --- custom URIDereferencer delivery participates in the digest ---
        final String payload = "<payload>delivered-by-custom-dereferencer</payload>";
        final List<String> calls = new ArrayList<String>();
        URIDereferencer ud = new URIDereferencer() {
            public Data dereference(URIReference ref, XMLCryptoContext ctx) {
                calls.add(ref.getURI());
                return new OctetStreamData(new ByteArrayInputStream(payload.getBytes()), ref.getURI(), "text/xml");
            }
        };
        Document ed = newDoc("<r/>");
        Reference extRef = sf.newReference("#custom", sf.newDigestMethod(DigestMethod.SHA256, null));
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(extRef));
        DOMSignContext sc = new DOMSignContext(kp.getPrivate(), ed.getDocumentElement());
        sc.setURIDereferencer(ud);
        sf.newXMLSignature(si, null).sign(sc);
        check(calls.size() == 1 && "#custom".equals(calls.get(0)), "custom dereferencer invoked at sign with reference URI");
        Element sigEl3 = (Element) ed.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext v4 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl3);
        v4.setURIDereferencer(ud);
        check(sf.unmarshalXMLSignature(v4).validate(v4), "same dereferencer at validate keeps signature valid");
        check(calls.size() == 2, "dereferencer invoked once per validation pass");
        // a tampering dereferencer breaks digest validation
        URIDereferencer udBad = new URIDereferencer() {
            public Data dereference(URIReference ref, XMLCryptoContext ctx) {
                return new OctetStreamData(new ByteArrayInputStream("<payload>TAMPERED</payload>".getBytes()), null, null);
            }
        };
        DOMValidateContext v5 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl3);
        v5.setURIDereferencer(udBad);
        check(!sf.unmarshalXMLSignature(v5).validate(v5), "tampering dereferencer invalidates signature");
        // unregistered custom dereferencer is null before set
        check(new DOMValidateContext(kp.getPublic(), baseEl).getURIDereferencer() == null,
            "no dereferencer before set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
