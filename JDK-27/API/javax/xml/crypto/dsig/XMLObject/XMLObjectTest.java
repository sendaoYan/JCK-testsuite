/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLObject conformance.
 * @run main XMLObjectTest
 */

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class XMLObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static KeyPair KP;
    static KeyPair rsaKP() throws Exception {
        if (KP == null) {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KP = kpg.generateKeyPair();
        }
        return KP;
    }

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLObject");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true;
        check(xmls, "implements XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getContent"), "has getContent");
        check(names.contains("getId"), "has getId");
        check(names.contains("getMimeType"), "has getMimeType");
        check(names.contains("getEncoding"), "has getEncoding");
    }

    static void testGetterMatrix() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        // all-null / empty content
        XMLObject o0 = sf.newXMLObject(null, null, null, null);
        check(o0.getContent() != null, "content list non-null even for null input");
        check(o0.getContent().isEmpty(), "content empty for null input");
        check(o0.getId() == null && o0.getMimeType() == null && o0.getEncoding() == null, "all-null metadata");
        // full form with mixed content
        Document d = newDoc("<r/>");
        DOMStructure txt = new DOMStructure(d.createTextNode("text-node"));
        DOMStructure el = new DOMStructure(d.createElementNS("urn:x", "inner"));
        XMLObject o1 = sf.newXMLObject(Arrays.asList(txt, el), "oid", "application/xml", "UTF-16");
        check(o1.getId().equals("oid"), "id round-trip");
        check(o1.getMimeType().equals("application/xml"), "mime round-trip");
        check(o1.getEncoding().equals("UTF-16"), "encoding round-trip");
        check(o1.getContent().size() == 2, "two content items");
        check(o1.getContent().get(0) == txt && o1.getContent().get(1) == el, "content identity and order");
        // content list immutable
        try { o1.getContent().add(txt); check(false, "content immutable"); }
        catch (UnsupportedOperationException e) { check(true, "content immutable UOE"); }
    }

    /** Enveloping form: object referenced by #id, sign -> validate -> tamper. */
    static void testEnvelopingClosedLoop() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r/>");
        Document payload = newDoc("<data xmlns=\"urn:payload\">V1</data>");
        XMLObject xo = sf.newXMLObject(
            Collections.singletonList((XMLStructure) new DOMStructure(payload.getDocumentElement())),
            "the-object", "text/xml", null);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("#the-object", dm);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        XMLSignature sig = sf.newXMLSignature(si, null, Collections.singletonList(xo), null, null);
        sig.sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        check(sig.getObjects().size() == 1, "object attached to signature");
        XMLObject back = (XMLObject) sig.getObjects().get(0);
        check(back.getId().equals("the-object"), "attached object id");
        check(back.getContent().size() == 1, "attached object content size");
        check(back.getContent().get(0) instanceof DOMStructure, "attached object content is DOMStructure");
        // marshalled document carries Object with Id/MimeType attributes and payload subtree
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        Element objEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Object").item(0);
        check(objEl.getAttribute("Id").equals("the-object"), "Object Id attribute");
        check(objEl.getAttribute("MimeType").equals("text/xml"), "Object MimeType attribute");
        check(objEl.getElementsByTagNameNS("urn:payload", "data").getLength() == 1, "payload subtree embedded");
        // validation
        DOMValidateContext vc = new DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "enveloping validation");
        XMLObject vo = (XMLObject) v.getObjects().get(0);
        check(vo.getId().equals("the-object"), "unmarshalled object id");
        check(vo.getContent().size() == 1, "unmarshalled object content size");
        // tamper payload -> invalid
        objEl.getElementsByTagNameNS("urn:payload", "data").item(0).setTextContent("V2");
        DOMValidateContext vc2 = new DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(!sf.unmarshalXMLSignature(vc2).validate(vc2), "object payload tamper detected");
    }

    /** Two objects each referenced separately; digest granularity per object. */
    static void testTwoObjectsGranularity() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r/>");
        XMLObject o1 = sf.newXMLObject(null, "g1", null, null);
        XMLObject o2 = sf.newXMLObject(null, "g2", null, null);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        List<Reference> refs = new ArrayList<Reference>();
        refs.add(sf.newReference("#g1", dm));
        refs.add(sf.newReference("#g2", dm));
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), refs);
        sf.newXMLSignature(si, null, Arrays.asList(o1, o2), null, null)
            .sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "two-object enveloping validates");
        // insert text into object g1 only
        Element g1 = (Element) ((Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Object").item(0));
        g1.appendChild(doc.createTextNode("junk"));
        DOMValidateContext vc2 = new DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v2 = sf.unmarshalXMLSignature(vc2);
        Reference r1 = (Reference) v2.getSignedInfo().getReferences().get(0);
        Reference r2 = (Reference) v2.getSignedInfo().getReferences().get(1);
        boolean r1v = r1.validate(vc2), r2v = r2.validate(vc2);
        check(r1v != r2v, "exactly one of the two object references invalidates after object mutation");
        check(!v2.validate(vc2), "overall invalid after object mutation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetterMatrix();
        testEnvelopingClosedLoop();
        testTwoObjectsGranularity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
