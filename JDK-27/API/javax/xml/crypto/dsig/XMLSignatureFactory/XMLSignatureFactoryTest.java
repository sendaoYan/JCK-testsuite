/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignatureFactory conformance: XML signature factory.
 * @run main XMLSignatureFactoryTest
 */

import javax.xml.crypto.KeySelector;
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
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.HMACParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dom.DOMStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class XMLSignatureFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    static String thrown(Throwable t) { return t.getClass().getSimpleName(); }

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

    /** Fresh enveloped signature over the whole document (objects are always built fresh). */
    static XMLSignature signEnveloped(XMLSignatureFactory sf, Document doc, Object keyOrSelector,
                                      String sigMethod, javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec spec) throws Exception {
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("", dm,
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(sigMethod, spec), Collections.singletonList(ref));
        XMLSignature sig = sf.newXMLSignature(si, null, null, null, null);
        DOMSignContext sc = new DOMSignContext((java.security.Key) keyOrSelector, doc.getDocumentElement());
        sig.sign(sc);
        return sig;
    }

    // ---- Part 1: white-box (existing structural checks) ----
    static void testWhiteBox() throws Exception {
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance();
        check(sf != null, "factory non-null");
        check(sf.getMechanismType() != null, "mechanism type");
        check(sf.getProvider() != null, "provider non-null");
        javax.xml.crypto.dsig.DigestMethod dm = sf.newDigestMethod("http://www.w3.org/2000/09/xmldsig#sha1", null);
        check(dm != null, "digest method non-null");
    }

    // ---- Part 2: algorithm URI constant table (RFC 4051 / xmldsig / xenc registries) ----
    static void testAlgorithmUriConstants() {
        check(DigestMethod.SHA1.equals("http://www.w3.org/2000/09/xmldsig#sha1"), "SHA1 URI");
        check(DigestMethod.SHA256.equals("http://www.w3.org/2001/04/xmlenc#sha256"), "SHA256 URI");
        check(DigestMethod.SHA512.equals("http://www.w3.org/2001/04/xmlenc#sha512"), "SHA512 URI");
        check(DigestMethod.RIPEMD160.equals("http://www.w3.org/2001/04/xmlenc#ripemd160"), "RIPEMD160 URI");
        check(javax.xml.crypto.dsig.SignatureMethod.DSA_SHA1.equals("http://www.w3.org/2000/09/xmldsig#dsa-sha1"), "DSA_SHA1 URI");
        check(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1.equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1"), "RSA_SHA1 URI");
        check(javax.xml.crypto.dsig.SignatureMethod.HMAC_SHA1.equals("http://www.w3.org/2000/09/xmldsig#hmac-sha1"), "HMAC_SHA1 URI");
        check(CanonicalizationMethod.INCLUSIVE.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315"), "INCLUSIVE URI");
        check(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"), "INCLUSIVE_WITH_COMMENTS URI");
        check(CanonicalizationMethod.EXCLUSIVE.equals("http://www.w3.org/2001/10/xml-exc-c14n#"), "EXCLUSIVE URI");
        check(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS.equals("http://www.w3.org/2001/10/xml-exc-c14n#WithComments"), "EXCLUSIVE_WITH_COMMENTS URI");
        check(Transform.BASE64.equals("http://www.w3.org/2000/09/xmldsig#base64"), "BASE64 URI");
        check(Transform.ENVELOPED.equals("http://www.w3.org/2000/09/xmldsig#enveloped-signature"), "ENVELOPED URI");
        check(Transform.XPATH.equals("http://www.w3.org/TR/1999/REC-xpath-19991116"), "XPATH URI");
        check(Transform.XPATH2.equals("http://www.w3.org/2002/06/xmldsig-filter2"), "XPATH2 URI");
        check(Transform.XSLT.equals("http://www.w3.org/TR/1999/REC-xslt-19991116"), "XSLT URI");
        check(XMLSignature.XMLNS.equals("http://www.w3.org/2000/09/xmldsig#"), "XMLNS URI");
    }

    // ---- Part 3: factory acquisition matrix + negatives ----
    static void testFactoryMatrix() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance();
        check("DOM".equals(sf.getMechanismType()), "default mechanism is DOM");
        check(sf.getProvider() != null, "provider non-null");
        XMLSignatureFactory sf2 = XMLSignatureFactory.getInstance("DOM");
        check("DOM".equals(sf2.getMechanismType()), "DOM mechanism");
        check(sf2.getProvider() != null, "DOM provider");
        XMLSignatureFactory sf3 = XMLSignatureFactory.getInstance("DOM", sf2.getProvider());
        check(sf3.getProvider() == sf2.getProvider(), "getInstance(mech, provider) provider identity");
        check("DOM".equals(sf3.getMechanismType()), "provider-overload mechanism");
        XMLSignatureFactory sf4 = XMLSignatureFactory.getInstance("DOM", sf2.getProvider().getName());
        check(sf4.getProvider().getName().equals(sf2.getProvider().getName()), "getInstance(mech, providerName)");
        // negatives
        try { XMLSignatureFactory.getInstance("Bogus"); check(false, "Bogus mechanism should fail"); }
        catch (javax.xml.crypto.NoSuchMechanismException e) {
            check(e.getMessage() != null && e.getMessage().contains("Bogus"), "NSME message contains mechanism name");
        }
        try { XMLSignatureFactory.getInstance(null); check(false, "null mechanism should fail"); }
        catch (NullPointerException e) { check(true, "null mechanism NPE"); }
        try { XMLSignatureFactory.getInstance("DOM", (Provider) null); check(false, "null provider should fail"); }
        catch (NullPointerException e) { check(true, "null provider NPE"); }
        try { XMLSignatureFactory.getInstance("DOM", "NoSuchProvXYZ"); check(false, "unknown provider should fail"); }
        catch (java.security.NoSuchProviderException e) { check(true, "unknown provider NoSuchProviderException"); }
        // feature support
        try { sf.isFeatureSupported(null); check(false, "isFeatureSupported(null) should NPE"); }
        catch (NullPointerException e) { check(true, "isFeatureSupported(null) NPE"); }
        check(!sf.isFeatureSupported("javax.xml.crypto.dsig.cacheReference"), "cacheReference unsupported by factory");
        check(!sf.isFeatureSupported("no.such.feature"), "unknown feature false");
        // dereferencer
        check(sf.getURIDereferencer() != null, "default URIDereferencer non-null");
        // keyinfo factory bridging
        KeyInfoFactory kif = sf.getKeyInfoFactory();
        check(kif != null, "getKeyInfoFactory non-null");
        check("DOM".equals(kif.getMechanismType()), "keyinfo factory mechanism DOM");
        check(kif.getProvider() == sf.getProvider(), "keyinfo factory same provider");
    }

    // ---- Part 4: new* method negative matrix (exception type + message) ----
    static void testNewMethodNegatives() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        javax.xml.crypto.dsig.SignatureMethod sm =
            sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null);
        CanonicalizationMethod cm =
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null);
        Reference r = sf.newReference("#x", dm);
        // unregistered digest algorithm
        try { sf.newDigestMethod("http://example.com/bogus-digest", null); check(false, "bogus digest should fail"); }
        catch (java.security.NoSuchAlgorithmException e) { check(e.getMessage() != null, "bogus digest NSA message"); }
        // digest with unexpected params
        try { sf.newDigestMethod(DigestMethod.SHA256, new DigestMethodParameterSpec() {}); check(false, "digest params should fail"); }
        catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage() != null && e.getMessage().contains("no parameters should be specified"), "digest params IAE message");
        }
        // signature method: unregistered
        try { sf.newSignatureMethod("http://example.com/bogus-sig", null); check(false, "bogus sigmethod should fail"); }
        catch (java.security.NoSuchAlgorithmException e) { check(e.getMessage() != null, "bogus sigmethod NSAE message"); }
        // signature method with params where none allowed (message names the URI)
        try { sf.newSignatureMethod(RSA_SHA256, new HMACParameterSpec(256)); check(false, "sigmethod params should fail"); }
        catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage() != null && e.getMessage().contains("rsa-sha256"), "sigmethod params IAE message contains URI");
        }
        // transform: unregistered (message contains the URI)
        try { sf.newTransform("http://example.com/bogus-transform", (TransformParameterSpec) null); check(false, "bogus transform should fail"); }
        catch (java.security.NoSuchAlgorithmException e) {
            check(e.getMessage() != null && e.getMessage().contains("http://example.com/bogus-transform"), "bogus transform NSAE message contains URI");
        }
        // transform XPATH requires params
        try { sf.newTransform(Transform.XPATH, (TransformParameterSpec) null); check(false, "XPATH without params should fail"); }
        catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage() != null && e.getMessage().contains("params are required"), "XPATH params IAE message");
        }
        // c14n with params where none allowed
        try { sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, new C14NMethodParameterSpec() {}); check(false, "c14n params should fail"); }
        catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage() != null && e.getMessage().contains("no parameters should be specified"), "c14n params IAE message");
        }
        // c14n unregistered
        try { sf.newCanonicalizationMethod("http://example.com/bogus-c14n", (C14NMethodParameterSpec) null); check(false, "bogus c14n should fail"); }
        catch (java.security.NoSuchAlgorithmException e) { check(true, "bogus c14n NSAE"); }
        // SignedInfo / Reference null + empty matrix
        try { sf.newSignedInfo(null, sm, Collections.singletonList(r)); check(false, "null cm"); }
        catch (NullPointerException e) { check(true, "null cm NPE"); }
        try { sf.newSignedInfo(cm, null, Collections.singletonList(r)); check(false, "null sm"); }
        catch (NullPointerException e) { check(true, "null sm NPE"); }
        try { sf.newSignedInfo(cm, sm, null); check(false, "null refs"); }
        catch (NullPointerException e) { check(true, "null refs NPE"); }
        try { sf.newSignedInfo(cm, sm, new ArrayList<Reference>()); check(false, "empty refs"); }
        catch (IllegalArgumentException e) { check(true, "empty refs IAE"); }
        try { sf.newSignedInfo(cm, sm, new ArrayList<Reference>(), null); check(false, "empty refs overload"); }
        catch (IllegalArgumentException e) { check(true, "empty refs overload IAE"); }
        try { sf.newReference("#x", null); check(false, "null digest method"); }
        catch (NullPointerException e) { check(true, "null DigestMethod NPE"); }
        try { sf.newXMLSignature(null, null); check(false, "null SignedInfo"); }
        catch (NullPointerException e) { check(true, "null SignedInfo NPE"); }
    }

    // ---- Part 5: RSA sign -> validate -> tamper three-state loop ----
    static void testRsaSignValidateTamperLoop() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<root xmlns=\"urn:demo\"><child>payload-A</child></root>");
        XMLSignature sig = signEnveloped(sf, doc, kp.getPrivate(), RSA_SHA256, null);
        // signature value materialized by sign()
        check(sig.getSignatureValue() != null, "SignatureValue non-null");
        check(sig.getSignatureValue().getValue() != null, "signature value bytes non-null");
        check(sig.getSignatureValue().getValue().length == 256, "RSA-2048 PKCS#1 v1.5 signature is 256 bytes, got " + sig.getSignatureValue().getValue().length);
        check(sig.getSignatureValue().getId() == null, "SignatureValue id null when not set");
        // digest value auto-computed at sign time
        Reference r0 = (Reference) sig.getSignedInfo().getReferences().get(0);
        check(r0.getDigestValue() != null && r0.getDigestValue().length == 32, "SHA-256 digest value 32 bytes");
        // marshalled tree structure
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        check(sigEl != null, "Signature element present");
        NodeList kids = sigEl.getChildNodes();
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < kids.getLength(); i++) {
            org.w3c.dom.Node n = kids.item(i);
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) names.add(n.getLocalName());
        }
        check(names.equals(Arrays.asList("SignedInfo", "SignatureValue")), "Signature children: " + names);
        Element siEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "SignedInfo").item(0);
        Element cmEl = (Element) siEl.getElementsByTagNameNS(XMLSignature.XMLNS, "CanonicalizationMethod").item(0);
        check(cmEl.getAttribute("Algorithm").equals(CanonicalizationMethod.INCLUSIVE), "marshalled c14n Algorithm attribute == constant");
        Element smEl = (Element) siEl.getElementsByTagNameNS(XMLSignature.XMLNS, "SignatureMethod").item(0);
        check(smEl.getAttribute("Algorithm").equals(RSA_SHA256), "marshalled SignatureMethod Algorithm == URI");
        Element refEl = (Element) siEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Reference").item(0);
        check(refEl.getAttribute("URI").equals(""), "marshalled Reference URI is empty string (whole doc)");
        Element dmEl = (Element) refEl.getElementsByTagNameNS(XMLSignature.XMLNS, "DigestMethod").item(0);
        check(dmEl.getAttribute("Algorithm").equals(DigestMethod.SHA256), "marshalled DigestMethod Algorithm == constant");
        Element trEl = (Element) refEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Transform").item(0);
        check(trEl.getAttribute("Algorithm").equals(Transform.ENVELOPED), "marshalled Transform Algorithm == constant");
        // validate (state 1: intact)
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v1 = sf.unmarshalXMLSignature(vc);
        check(v1 != sig, "unmarshalled signature is a distinct object");
        check(v1.validate(vc), "intact signature validates");
        Reference vr = (Reference) v1.getSignedInfo().getReferences().get(0);
        check(vr.validate(vc), "intact reference validates");
        check(v1.getSignatureValue().validate(vc), "intact SignatureValue validates");
        check(Arrays.equals(vr.getDigestValue(), vr.getCalculatedDigestValue()), "stored digest == calculated digest");
        check(v1.getSignedInfo().getSignatureMethod().getAlgorithm().equals(RSA_SHA256), "unmarshalled SignatureMethod round-trip");
        // unmarshal dual: (XMLStructure) overload from the same element
        XMLSignature v1b = sf.unmarshalXMLSignature((XMLStructure) new DOMStructure(sigEl));
        check(v1b.getSignedInfo() != null, "XMLStructure overload unmarshals SignedInfo");
        check(v1b.getId() == null, "no id set");
        // tamper (state 2: DigestValue byte flip) - validation must go false
        NodeList dvl = sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "DigestValue");
        String dv = dvl.item(0).getTextContent();
        dvl.item(0).setTextContent((dv.charAt(0) == 'A' ? 'B' : 'A') + dv.substring(1));
        DOMValidateContext vc2 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v2 = sf.unmarshalXMLSignature(vc2);
        check(!v2.validate(vc2), "tampered DigestValue -> signature invalid");
        check(!((Reference) v2.getSignedInfo().getReferences().get(0)).validate(vc2), "tampered DigestValue -> reference invalid");
        // tamper (state 3: signed content) on a fresh document
        Document doc2 = newDoc("<root xmlns=\"urn:demo\"><child>payload-A</child></root>");
        signEnveloped(sf, doc2, kp.getPrivate(), RSA_SHA256, null);
        Element sigEl2 = (Element) doc2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        doc2.getElementsByTagNameNS("urn:demo", "child").item(0).setTextContent("payload-B");
        DOMValidateContext vc3 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl2);
        XMLSignature v3 = sf.unmarshalXMLSignature(vc3);
        check(!v3.validate(vc3), "tampered content -> signature invalid");
        check(!((Reference) v3.getSignedInfo().getReferences().get(0)).validate(vc3), "tampered content -> reference invalid");
        check(v3.getSignatureValue().validate(vc3), "content tamper: crypto signature over SignedInfo still cryptographically valid");
        // tamper (state 4: SignatureValue byte flip) - reference stays valid, crypto fails
        Document doc3 = newDoc("<root xmlns=\"urn:demo\"><child>payload-A</child></root>");
        signEnveloped(sf, doc3, kp.getPrivate(), RSA_SHA256, null);
        Element sigEl3 = (Element) doc3.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        NodeList svl = sigEl3.getElementsByTagNameNS(XMLSignature.XMLNS, "SignatureValue");
        String sv = svl.item(0).getTextContent();
        svl.item(0).setTextContent((sv.charAt(0) == 'A' ? 'B' : 'A') + sv.substring(1));
        DOMValidateContext vc4 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl3);
        XMLSignature v4 = sf.unmarshalXMLSignature(vc4);
        check(!v4.validate(vc4), "tampered SignatureValue -> signature invalid");
        check(!v4.getSignatureValue().validate(vc4), "tampered SignatureValue -> crypto invalid");
        check(((Reference) v4.getSignedInfo().getReferences().get(0)).validate(vc4), "reference digest still valid after SignatureValue tamper");
        // unmarshal negative: non-Signature element
        Document doc5 = newDoc("<r/>");
        try {
            sf.unmarshalXMLSignature((XMLStructure) new DOMStructure(doc5.createElement("bogus")));
            check(false, "unmarshal of bogus element should fail");
        } catch (javax.xml.crypto.MarshalException e) { check(true, "unmarshal bogus MarshalException"); }
    }

    // ---- Part 6: HMAC sign loop + HMACOutputLength contract ----
    static void testHmacLoop() throws Exception {
        SecretKey hk = KeyGenerator.getInstance("HmacSHA256").generateKey();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r><c>hm</c></r>");
        signEnveloped(sf, doc, hk, HMAC_SHA256, null);
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(hk), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "HMAC-SHA256 round-trip validates");
        // default output length = full 256 bits = 32 bytes
        Document doc2 = newDoc("<r><c>hm</c></r>");
        XMLSignature h2 = signEnveloped(sf, doc2, hk, HMAC_SHA256, null);
        check(h2.getSignatureValue().getValue().length == 32, "default HMAC output 32 bytes");
        // explicit HMACParameterSpec(256): still 32 bytes
        Document doc3 = newDoc("<r><c>hm</c></r>");
        XMLSignature h3 = signEnveloped(sf, doc3, hk, HMAC_SHA256, new HMACParameterSpec(256));
        check(h3.getSignatureValue().getValue().length == 32, "HMACParameterSpec(256) -> 32 bytes");
        Element el3 = (Element) doc3.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc3 = new DOMValidateContext(KeySelector.singletonKeySelector(hk), el3);
        check(sf.unmarshalXMLSignature(vc3).validate(vc3), "HMACParameterSpec(256) round-trip validates");
        // output below 256 bits rejected at sign time (Santuario hardening)
        Document doc4 = newDoc("<r><c>hm</c></r>");
        try {
            signEnveloped(sf, doc4, hk, HMAC_SHA256, new HMACParameterSpec(128));
            check(false, "HMACOutputLength 128 must be rejected");
        } catch (javax.xml.crypto.dsig.XMLSignatureException e) {
            check(e.getMessage() != null && e.getMessage().contains("HMACOutputLength") && e.getMessage().contains("256"),
                "HMACOutputLength rejection message: " + e.getMessage());
        }
    }

    // ---- Part 7: enveloping form (XMLObject referenced by #id) ----
    static void testEnvelopingForm() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r/>");
        Document payload = newDoc("<data xmlns=\"urn:obj\">O1</data>");
        javax.xml.crypto.dsig.XMLObject xo = sf.newXMLObject(
            Collections.singletonList((XMLStructure) new DOMStructure(payload.getDocumentElement())),
            "obj1", "text/plain", "UTF-8");
        Reference ref = sf.newReference("#obj1", sf.newDigestMethod(DigestMethod.SHA256, null));
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        XMLSignature sig = sf.newXMLSignature(si, null, Collections.singletonList(xo), "sig0", null);
        sig.sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        check(sig.getId().equals("sig0"), "signature id round-trip");
        check(sig.getObjects().size() == 1, "one embedded object");
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        Element objEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Object").item(0);
        check(objEl.getAttribute("Id").equals("obj1"), "Object Id attribute marshalled");
        check(objEl.getAttribute("MimeType").equals("text/plain"), "Object MimeType attribute marshalled");
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "enveloping signature validates");
        check(v.getObjects().size() == 1, "unmarshalled enveloping objects size 1");
        check(((javax.xml.crypto.dsig.XMLObject) v.getObjects().get(0)).getId().equals("obj1"), "unmarshalled object id");
        // tamper the enveloped object payload
        objEl.getElementsByTagNameNS("urn:obj", "data").item(0).setTextContent("EVIL");
        DOMValidateContext vc2 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(!sf.unmarshalXMLSignature(vc2).validate(vc2), "enveloping object tamper detected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testAlgorithmUriConstants();
        testFactoryMatrix();
        testNewMethodNegatives();
        testRsaSignValidateTamperLoop();
        testHmacLoop();
        testEnvelopingForm();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
