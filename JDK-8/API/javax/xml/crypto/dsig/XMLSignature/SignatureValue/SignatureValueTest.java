/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignature.SignatureValue conformance.
 * @run main SignatureValueTest
 */

import javax.xml.crypto.KeySelector;
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
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Collections;

public class SignatureValueTest {
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
    static final String HMAC_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha256";

    static XMLSignature signEnveloped(XMLSignatureFactory sf, Document doc, java.security.Key key, String method) throws Exception {
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("", dm,
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(method, null), Collections.singletonList(ref));
        XMLSignature sig = sf.newXMLSignature(si, null, null, null, "sv-id");
        sig.sign(new DOMSignContext(key, doc.getDocumentElement()));
        return sig;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLSignature$SignatureValue");
        check(c.isInterface(), "is interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getId"), "has method getId");
        check(names.contains("getValue"), "has method getValue");
        check(names.contains("validate"), "has method validate");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean ok = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.XMLStructure")) ok = true;
        check(ok, "implements XMLStructure");
    }

    static void testValueLifecycle() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r>V</r>");
        XMLSignature sig = signEnveloped(sf, doc, kp.getPrivate(), RSA_SHA256);
        XMLSignature.SignatureValue sv = sig.getSignatureValue();
        check(sv != null, "SignatureValue non-null");
        check("sv-id".equals(sv.getId()), "id round-trip (signatureValueId parameter)");
        byte[] v1 = sv.getValue();
        check(v1 != null, "value bytes non-null after sign");
        check(v1.length == 256, "RSA-2048 value is 256 bytes, got " + v1.length);
        // PKCS#1 v1.5 RSA is deterministic: re-signing identical input yields identical bytes
        Document doc2 = newDoc("<r>V</r>");
        XMLSignature sig2 = signEnveloped(sf, doc2, kp.getPrivate(), RSA_SHA256);
        check(Arrays.equals(v1, sig2.getSignatureValue().getValue()), "deterministic RSA PKCS#1 v1.5 over identical input");
        // different signed content -> different signature bytes
        Document doc3 = newDoc("<r>W</r>");
        XMLSignature sig3 = signEnveloped(sf, doc3, kp.getPrivate(), RSA_SHA256);
        check(!Arrays.equals(v1, sig3.getSignatureValue().getValue()), "content change alters signature value");
        // getValue returns a fresh copy each call (defensive)
        byte[] c1 = sv.getValue();
        byte[] c2 = sv.getValue();
        check(c1 != c2 && Arrays.equals(c1, c2), "getValue defensive copies");
        c1[0] = (byte) ~c1[0];
        check(sv.getValue()[0] != c1[0], "mutating returned copy does not corrupt internal state");
    }

    static void testValidateStates() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r>V</r>");
        signEnveloped(sf, doc, kp.getPrivate(), RSA_SHA256);
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.getSignatureValue().validate(vc), "intact value validates");
        // flip one base64 character of SignatureValue
        NodeList svl = sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "SignatureValue");
        String txt = svl.item(0).getTextContent();
        svl.item(0).setTextContent((txt.charAt(0) == 'A' ? 'B' : 'A') + txt.substring(1));
        DOMValidateContext vc2 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v2 = sf.unmarshalXMLSignature(vc2);
        check(!v2.getSignatureValue().validate(vc2), "flipped value fails crypto validation");
        check(!v2.validate(vc2), "overall validation false");
        check(((Reference) v2.getSignedInfo().getReferences().get(0)).validate(vc2), "reference digest independent of crypto failure");
    }

    static void testHmacValueShape() throws Exception {
        javax.crypto.SecretKey hk = javax.crypto.KeyGenerator.getInstance("HmacSHA256").generateKey();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r>H</r>");
        XMLSignature sig = signEnveloped(sf, doc, hk, HMAC_SHA256);
        check(sig.getSignatureValue().getValue().length == 32, "HMAC-SHA256 value 32 bytes (full length)");
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(hk), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "HMAC value validates");
        check(sf.unmarshalXMLSignature(vc).getSignatureValue().getValue().length == 32, "unmarshalled HMAC value length");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testValueLifecycle();
        testValidateStates();
        testHmacValueShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
