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


    /** JDK-27-only: the full algorithm-constant table added since JDK 8. */
    static void testJdk27ConstantTable() {
        check(SignatureMethod.DSA_SHA256.equals("http://www.w3.org/2009/xmldsig11#dsa-sha256"), "DSA_SHA256 URI");
        check(SignatureMethod.RSA_SHA224.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha224"), "RSA_SHA224 URI");
        check(SignatureMethod.RSA_SHA256.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"), "RSA_SHA256 URI");
        check(SignatureMethod.RSA_SHA384.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384"), "RSA_SHA384 URI");
        check(SignatureMethod.RSA_SHA512.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"), "RSA_SHA512 URI");
        check(SignatureMethod.SHA1_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha1-rsa-MGF1"), "SHA1_RSA_MGF1 URI");
        check(SignatureMethod.SHA224_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha224-rsa-MGF1"), "SHA224_RSA_MGF1 URI");
        check(SignatureMethod.SHA256_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1"), "SHA256_RSA_MGF1 URI");
        check(SignatureMethod.SHA384_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha384-rsa-MGF1"), "SHA384_RSA_MGF1 URI");
        check(SignatureMethod.SHA512_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1"), "SHA512_RSA_MGF1 URI");
        check(SignatureMethod.ECDSA_SHA1.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1"), "ECDSA_SHA1 URI");
        check(SignatureMethod.ECDSA_SHA224.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224"), "ECDSA_SHA224 URI");
        check(SignatureMethod.ECDSA_SHA256.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256"), "ECDSA_SHA256 URI");
        check(SignatureMethod.ECDSA_SHA384.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384"), "ECDSA_SHA384 URI");
        check(SignatureMethod.ECDSA_SHA512.equals("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512"), "ECDSA_SHA512 URI");
        check(SignatureMethod.HMAC_SHA224.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha224"), "HMAC_SHA224 URI");
        check(SignatureMethod.HMAC_SHA256.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256"), "HMAC_SHA256 URI");
        check(SignatureMethod.HMAC_SHA384.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha384"), "HMAC_SHA384 URI");
        check(SignatureMethod.HMAC_SHA512.equals("http://www.w3.org/2001/04/xmldsig-more#hmac-sha512"), "HMAC_SHA512 URI");
        check(SignatureMethod.RSA_PSS.equals("http://www.w3.org/2007/05/xmldsig-more#rsa-pss"), "RSA_PSS URI");
        check(SignatureMethod.ED25519.equals("http://www.w3.org/2021/04/xmldsig-more#eddsa-ed25519"), "ED25519 URI");
        check(SignatureMethod.ED448.equals("http://www.w3.org/2021/04/xmldsig-more#eddsa-ed448"), "ED448 URI");
        check(SignatureMethod.SHA3_224_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-224-rsa-MGF1"), "SHA3_224_RSA_MGF1 URI");
        check(SignatureMethod.SHA3_256_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-256-rsa-MGF1"), "SHA3_256_RSA_MGF1 URI");
        check(SignatureMethod.SHA3_384_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-384-rsa-MGF1"), "SHA3_384_RSA_MGF1 URI");
        check(SignatureMethod.SHA3_512_RSA_MGF1.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-512-rsa-MGF1"), "SHA3_512_RSA_MGF1 URI");
        check(SignatureMethod.ECDSA_SHA3_224.equals("http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-224"), "ECDSA_SHA3_224 URI");
        check(SignatureMethod.ECDSA_SHA3_256.equals("http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-256"), "ECDSA_SHA3_256 URI");
        check(SignatureMethod.ECDSA_SHA3_384.equals("http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-384"), "ECDSA_SHA3_384 URI");
        check(SignatureMethod.ECDSA_SHA3_512.equals("http://www.w3.org/2021/04/xmldsig-more#ecdsa-sha3-512"), "ECDSA_SHA3_512 URI");
    }

    /** JDK-27-only: constants unknown to JDK 8 are resolvable algorithm methods here. */
    static void testJdk27Families() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        String[] uris = {
            SignatureMethod.RSA_SHA256, SignatureMethod.RSA_SHA384, SignatureMethod.RSA_SHA512,
            SignatureMethod.DSA_SHA256, SignatureMethod.HMAC_SHA256, SignatureMethod.HMAC_SHA384,
            SignatureMethod.HMAC_SHA512, SignatureMethod.ECDSA_SHA384, SignatureMethod.ECDSA_SHA512,
            SignatureMethod.SHA256_RSA_MGF1, SignatureMethod.SHA384_RSA_MGF1, SignatureMethod.SHA512_RSA_MGF1,
            SignatureMethod.SHA3_256_RSA_MGF1, SignatureMethod.ECDSA_SHA3_256,
        };
        for (String uri : uris) {
            check(sf.newSignatureMethod(uri, null) != null, "resolvable: " + uri);
        }
        // RSA_PSS resolvable both with and without explicit parameters
        check(sf.newSignatureMethod(SignatureMethod.RSA_PSS, null) != null, "RSA_PSS with null params");
        java.security.spec.PSSParameterSpec inner = new java.security.spec.PSSParameterSpec("SHA-256", "MGF1",
            new java.security.spec.MGF1ParameterSpec("SHA-256"), 32, 1);
        check(sf.newSignatureMethod(SignatureMethod.RSA_PSS,
            new javax.xml.crypto.dsig.spec.RSAPSSParameterSpec(inner)) != null, "RSA_PSS with RSAPSSParameterSpec");
        // RSA-PSS family full sign/validate round-trip
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        java.security.KeyPair kp = kpg.generateKeyPair();
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod(SignatureMethod.RSA_PSS,
                new javax.xml.crypto.dsig.spec.RSAPSSParameterSpec(inner)),
            java.util.Collections.singletonList(sf.newReference("",
                sf.newDigestMethod(DigestMethod.SHA256, null),
                java.util.Collections.singletonList(sf.newTransform(javax.xml.crypto.dsig.Transform.ENVELOPED,
                    (javax.xml.crypto.dsig.spec.TransformParameterSpec) null)), null, null)));
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        org.w3c.dom.Element root = d.createElement("r");
        d.appendChild(root);
        sf.newXMLSignature(si, null).sign(new javax.xml.crypto.dsig.dom.DOMSignContext(kp.getPrivate(), root));
        org.w3c.dom.Element sigEl = (org.w3c.dom.Element)
            root.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature").item(0);
        javax.xml.crypto.dsig.dom.DOMValidateContext vc = new javax.xml.crypto.dsig.dom.DOMValidateContext(
            javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "RSA-PSS sign/validate round-trip");
        // HMAC SHA-384/512 families sign and validate with correct output lengths
        javax.crypto.SecretKey hk384 = new javax.crypto.spec.SecretKeySpec(new byte[48], "HmacSHA384");
        javax.crypto.SecretKey hk512 = new javax.crypto.spec.SecretKeySpec(new byte[64], "HmacSHA512");
        for (javax.crypto.SecretKey sk : new javax.crypto.SecretKey[] {hk384, hk512}) {
            javax.xml.crypto.dsig.SignedInfo hsi = sf.newSignedInfo(
                sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                    (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
                sf.newSignatureMethod(sk.getAlgorithm().equals("HmacSHA384")
                    ? SignatureMethod.HMAC_SHA384 : SignatureMethod.HMAC_SHA512, null),
                java.util.Collections.singletonList(sf.newReference("#h",
                    sf.newDigestMethod(DigestMethod.SHA256, null))));
            org.w3c.dom.Document hd = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
            org.w3c.dom.Element hroot = hd.createElement("r");
            hd.appendChild(hroot);
            sf.newXMLSignature(hsi, null,
                java.util.Collections.singletonList(sf.newXMLObject(null, "h", null, null)), null, null)
                .sign(new javax.xml.crypto.dsig.dom.DOMSignContext(sk, hroot));
            org.w3c.dom.Element hsig = (org.w3c.dom.Element)
                hroot.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature").item(0);
            javax.xml.crypto.dsig.dom.DOMValidateContext hvc = new javax.xml.crypto.dsig.dom.DOMValidateContext(
                javax.xml.crypto.KeySelector.singletonKeySelector(sk), hsig);
            check(sf.unmarshalXMLSignature(hvc).validate(hvc), sk.getAlgorithm() + " round-trip");
            int len = sf.unmarshalXMLSignature(hvc).getSignatureValue().getValue().length;
            check(len == (sk.getAlgorithm().equals("HmacSHA384") ? 48 : 64),
                sk.getAlgorithm() + " output length " + len);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstants();
        testInstantiationAndSpecRoundTrip();
        testMethodFamiliesRoundTrip();
        testJdk27ConstantTable();
        testJdk27Families();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
