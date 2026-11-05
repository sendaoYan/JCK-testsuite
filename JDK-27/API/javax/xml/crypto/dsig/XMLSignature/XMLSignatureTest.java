/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignature conformance: interface hierarchy and methods.
 * @run main XMLSignatureTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dom.DOMStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class XMLSignatureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
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

    static XMLSignatureFactory sf() { return XMLSignatureFactory.getInstance("DOM"); }

    static SignedInfo envelopedSI(XMLSignatureFactory sf) throws Exception {
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("", dm,
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        return sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLSignature");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.XMLStructure"), "implements javax.xml.crypto.XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("validate"), "has method validate");
        check(names.contains("getKeyInfo"), "has method getKeyInfo");
        check(names.contains("getSignedInfo"), "has method getSignedInfo");
        check(names.contains("getObjects"), "has method getObjects");
        check(names.contains("getId"), "has method getId");
        check(names.contains("getSignatureValue"), "has method getSignatureValue");
        check(names.contains("sign"), "has method sign");
        check(names.contains("getKeySelectorResult"), "has method getKeySelectorResult");
        // XMLNS constant
        check(XMLSignature.XMLNS.equals("http://www.w3.org/2000/09/xmldsig#"), "XMLNS constant value");
    }

    /** Enveloped signature with KeyInfo KeyValue: key delivered via the KeyInfo channel. */
    static void testStructureAndGetters() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = sf();
        Document doc = newDoc("<root xmlns=\"urn:g\"><a>data</a></root>");
        KeyInfoFactory kif = sf.getKeyInfoFactory();
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kif.newKeyValue(kp.getPublic())), "ki0");
        XMLSignature sig = sf.newXMLSignature(envelopedSI(sf), ki, null, "sid", "svid");
        check(sig.getId().equals("sid"), "id round-trip");
        check(sig.getKeyInfo() == ki, "KeyInfo identity");
        check(sig.getSignedInfo() != null, "SignedInfo non-null");
        check(sig.getSignatureValue() != null, "SignatureValue handle non-null before sign");
        check(sig.getObjects().isEmpty(), "no objects");
        check(sig.getKeySelectorResult() == null, "no selector result before validation");
        sig.sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        check(sigEl.getAttribute("Id").equals("sid"), "marshalled Signature Id attribute");
        // SignatureValue id marshalled as attribute of SignatureValue element
        Element svEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "SignatureValue").item(0);
        check(svEl.getAttribute("Id").equals("svid"), "marshalled SignatureValue Id attribute");
        Element kiEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo").item(0);
        check(kiEl.getAttribute("Id").equals("ki0"), "marshalled KeyInfo Id attribute");
        check(sigEl.getChildNodes().getLength() >= 3, "Signature element children present");
        // validate with a selector reading the key out of KeyInfo (KeyValue channel)
        DOMValidateContext vc = new DOMValidateContext(new KeyValueSelector(), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "KeyInfo-channel key delivery validates");
        KeySelectorResult ksr = v.getKeySelectorResult();
        check(ksr != null && ksr.getKey().equals(kp.getPublic()), "getKeySelectorResult returns validation key");
    }

    static class KeyValueSelector extends javax.xml.crypto.KeySelector {
        public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, javax.xml.crypto.AlgorithmMethod method,
                                        javax.xml.crypto.XMLCryptoContext context) throws javax.xml.crypto.KeySelectorException {
            if (keyInfo != null) {
                for (Object o : keyInfo.getContent()) {
                    if (o instanceof KeyValue) {
                        try {
                            final java.security.PublicKey pk = ((KeyValue) o).getPublicKey();
                            return new KeySelectorResult() { public java.security.Key getKey() { return pk; } };
                        } catch (KeyException e) { throw new javax.xml.crypto.KeySelectorException(e); }
                    }
                }
            }
            throw new javax.xml.crypto.KeySelectorException("no KeyValue found");
        }
    }

    /** Validation state machine: validate() flips overall + per-component status. */
    static void testValidationStates() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = sf();
        Document doc = newDoc("<root xmlns=\"urn:g\">X</root>");
        XMLSignature sig = sf.newXMLSignature(envelopedSI(sf), null);
        sig.sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "intact validates");
        check(v.getSignatureValue().validate(vc), "sig value validates");
        Reference r = (Reference) v.getSignedInfo().getReferences().get(0);
        check(r.validate(vc), "reference validates");
        // mutate content -> reference false, overall false
        doc.getDocumentElement().setTextContent("Y");
        DOMValidateContext vc2 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v2 = sf.unmarshalXMLSignature(vc2);
        check(!v2.validate(vc2), "tampered content invalid");
        check(!((Reference) v2.getSignedInfo().getReferences().get(0)).validate(vc2), "tampered content reference invalid");
        // wrong key -> crypto invalid, reference still valid (fresh intact document)
        KeyPairGenerator opg = KeyPairGenerator.getInstance("RSA");
        opg.initialize(2048);
        KeyPair other = opg.generateKeyPair();
        Document docFresh = newDoc("<root xmlns=\"urn:g\">X</root>");
        sf.newXMLSignature(envelopedSI(sf), null).sign(new DOMSignContext(kp.getPrivate(), docFresh.getDocumentElement()));
        Element sigElF = (Element) docFresh.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc3 = new DOMValidateContext(KeySelector.singletonKeySelector(other.getPublic()), sigElF);
        XMLSignature v3 = sf.unmarshalXMLSignature(vc3);
        check(!v3.validate(vc3), "wrong key invalid");
        check(!v3.getSignatureValue().validate(vc3), "wrong key crypto invalid");
        check(((Reference) v3.getSignedInfo().getReferences().get(0)).validate(vc3), "wrong key reference still valid");
    }

    /** Multiple objects + manifest-style content enumerated through getObjects(). */
    static void testObjectsEnumeration() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = sf();
        Document doc = newDoc("<r/>");
        javax.xml.crypto.dsig.XMLObject o1 = sf.newXMLObject(null, "o1", null, null);
        javax.xml.crypto.dsig.XMLObject o2 = sf.newXMLObject(null, "o2", null, null);
        List<Reference> refs = new ArrayList<Reference>();
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        refs.add(sf.newReference("#o1", dm));
        refs.add(sf.newReference("#o2", dm));
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), refs);
        XMLSignature sig = sf.newXMLSignature(si, null, Arrays.asList(o1, o2), null, null);
        sig.sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        check(sig.getObjects().size() == 2, "two objects");
        boolean saw1 = false, saw2 = false;
        List<String> ids = new ArrayList<String>();
        for (Object o : sig.getObjects()) ids.add(((javax.xml.crypto.dsig.XMLObject) o).getId());
        check(ids.contains("o1") && ids.contains("o2"), "object ids enumerated: " + ids);
        saw1 = ids.contains("o1"); saw2 = ids.contains("o2");
        check(saw1 && saw2, "both ids present");
        try { sig.getObjects().add(o1); check(false, "objects list must be immutable"); }
        catch (UnsupportedOperationException e) { check(true, "objects list immutable"); }
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "multi-object enveloping validates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testStructureAndGetters();
        testValidationStates();
        testObjectsEnumeration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
