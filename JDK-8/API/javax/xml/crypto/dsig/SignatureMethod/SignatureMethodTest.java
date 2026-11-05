/*
 * @test
 * @summary javax.xml.crypto.dsig.SignatureMethod conformance.
 * @run main SignatureMethodTest
 */

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.HMACParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Collections;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SignatureMethodTest {
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
    static final String RSA_SHA384 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384";
    static final String RSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
    static final String HMAC_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha256";
    static final String HMAC_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha512";

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.SignatureMethod");
        check(c.isInterface(), "is interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getParameterSpec"), "has getParameterSpec");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false, am = false;
        for (String i : ifs) { if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true; if (i.equals("javax.xml.crypto.AlgorithmMethod")) am = true; }
        check(xmls && am, "implements XMLStructure and AlgorithmMethod");
    }

    static void testConstants() {
        check(SignatureMethod.DSA_SHA1.equals("http://www.w3.org/2000/09/xmldsig#dsa-sha1"), "DSA_SHA1 constant");
        check(SignatureMethod.RSA_SHA1.equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1"), "RSA_SHA1 constant");
        check(SignatureMethod.HMAC_SHA1.equals("http://www.w3.org/2000/09/xmldsig#hmac-sha1"), "HMAC_SHA1 constant");
    }

    static void testInstantiationAndSpecRoundTrip() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        // RSA methods carry no parameter spec
        for (String uri : new String[] {SignatureMethod.RSA_SHA1, RSA_SHA256, RSA_SHA384, RSA_SHA512}) {
            SignatureMethod m = sf.newSignatureMethod(uri, null);
            check(m.getAlgorithm().equals(uri), "getAlgorithm round-trip " + uri);
            check(m.getParameterSpec() == null, "no spec for " + uri);
        }
        // HMAC method with and without HMACParameterSpec; spec round-trips through getParameterSpec
        SignatureMethod h1 = sf.newSignatureMethod(HMAC_SHA256, null);
        check(h1.getAlgorithm().equals(HMAC_SHA256), "hmac algorithm");
        check(h1.getParameterSpec() == null, "hmac no spec");
        HMACParameterSpec spec = new HMACParameterSpec(256);
        SignatureMethod h2 = sf.newSignatureMethod(HMAC_SHA256, spec);
        check(h2.getParameterSpec() == spec, "HMACParameterSpec identity round-trip");
        check(((HMACParameterSpec) h2.getParameterSpec()).getOutputLength() == 256, "spec output length");
        // DSA method
        SignatureMethod d1 = sf.newSignatureMethod(SignatureMethod.DSA_SHA1, null);
        check(d1.getAlgorithm().equals(SignatureMethod.DSA_SHA1), "dsa algorithm");
        // spec where none allowed
        try { sf.newSignatureMethod(RSA_SHA256, new HMACParameterSpec(256)); check(false, "rsa with hmac spec"); }
        catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage().contains("rsa-sha256"), "IAE message names the URI");
        }
        try { sf.newSignatureMethod("http://example.com/none", null); check(false, "unregistered method"); }
        catch (java.security.NoSuchAlgorithmException e) { check(true, "unregistered method NSA"); }
    }

    /** End-to-end loop per signature-method family (RSA + HMAC), incl. truncation via HMACParameterSpec. */
    static void testMethodFamiliesRoundTrip() throws Exception {
        KeyPair kp = rsaKP();
        SecretKey hk = KeyGenerator.getInstance("HmacSHA512").generateKey();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        // RSA-SHA256 / RSA-SHA512
        for (String uri : new String[] {RSA_SHA256, RSA_SHA512}) {
            Document doc = newDoc("<r>M</r>");
            signWith(sf, doc, kp.getPrivate(), uri, null);
            Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
            javax.xml.crypto.dsig.dom.DOMValidateContext vc =
                new javax.xml.crypto.dsig.dom.DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
            check(sf.unmarshalXMLSignature(vc).validate(vc), uri + " round-trip validates");
            check(sf.unmarshalXMLSignature(vc).getSignedInfo().getSignatureMethod().getAlgorithm().equals(uri),
                uri + " unmarshalled method URI");
        }
        // HMAC-SHA512: default 64-byte value; explicit HMACParameterSpec(512) -> 64 bytes
        Document d1 = newDoc("<r>M</r>");
        XMLSignature s1 = signWith(sf, d1, hk, HMAC_SHA512, null);
        check(s1.getSignatureValue().getValue().length == 64, "hmac-sha512 default 64 bytes");
        Document d2 = newDoc("<r>M</r>");
        XMLSignature s2 = signWith(sf, d2, hk, HMAC_SHA512, new HMACParameterSpec(512));
        check(s2.getSignatureValue().getValue().length == 64, "HMACParameterSpec(512) -> 64 bytes (== outputLength/8)");
        Element el2 = (Element) d2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        javax.xml.crypto.dsig.dom.DOMValidateContext vc2 =
            new javax.xml.crypto.dsig.dom.DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(hk), el2);
        check(sf.unmarshalXMLSignature(vc2).validate(vc2), "explicit-output hmac round-trip validates");
        // explicit output length marshalled as HMACOutputLength parameter
        Element smEl = (Element) el2.getElementsByTagNameNS(XMLSignature.XMLNS, "SignatureMethod").item(0);
        org.w3c.dom.NodeList pms = smEl.getElementsByTagNameNS(XMLSignature.XMLNS, "HMACOutputLength");
        check(pms.getLength() == 1 && pms.item(0).getTextContent().equals("512"), "HMACOutputLength parameter marshalled as 512");
        // output below the HMAC digest length is rejected at sign time
        Document d3 = newDoc("<r>M</r>");
        try { signWith(sf, d3, hk, HMAC_SHA512, new HMACParameterSpec(256)); check(false, "below-minimum HMACOutputLength must throw"); }
        catch (javax.xml.crypto.dsig.XMLSignatureException e) {
            check(e.getMessage().contains("HMACOutputLength") && e.getMessage().contains("512"),
                "HMACOutputLength minimum message: " + e.getMessage());
        }
    }

    static XMLSignature signWith(XMLSignatureFactory sf, Document doc, java.security.Key key, String method,
                                 SignatureMethodParameterSpec spec) throws Exception {
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("", dm,
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(method, spec), Collections.singletonList(ref));
        XMLSignature sig = sf.newXMLSignature(si, null);
        sig.sign(new DOMSignContext(key, doc.getDocumentElement()));
        return sig;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstants();
        testInstantiationAndSpecRoundTrip();
        testMethodFamiliesRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
