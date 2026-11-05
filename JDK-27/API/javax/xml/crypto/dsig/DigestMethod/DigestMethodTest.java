/*
 * @test
 * @summary javax.xml.crypto.dsig.DigestMethod conformance.
 * @run main DigestMethodTest
 */

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Collections;

public class DigestMethodTest {
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
    static final String SHA384 = "http://www.w3.org/2001/04/xmldsig-more#sha384";
    static final String SHA224 = "http://www.w3.org/2001/04/xmldsig-more#sha224";

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.DigestMethod");
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

    static void testConstantsAndInstances() throws Exception {
        check(DigestMethod.SHA1.equals("http://www.w3.org/2000/09/xmldsig#sha1"), "SHA1 constant");
        check(DigestMethod.SHA256.equals("http://www.w3.org/2001/04/xmlenc#sha256"), "SHA256 constant");
        check(DigestMethod.SHA512.equals("http://www.w3.org/2001/04/xmlenc#sha512"), "SHA512 constant");
        check(DigestMethod.RIPEMD160.equals("http://www.w3.org/2001/04/xmlenc#ripemd160"), "RIPEMD160 constant");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        // every constant URI instantiable through the factory, getAlgorithm round-trips
        for (String uri : new String[] {DigestMethod.SHA1, DigestMethod.SHA256, DigestMethod.SHA512, DigestMethod.RIPEMD160,
                SHA384, SHA224}) {
            DigestMethod dm = sf.newDigestMethod(uri, null);
            check(dm.getAlgorithm().equals(uri), "getAlgorithm round-trip for " + uri);
            check(dm.getParameterSpec() == null, "no parameter spec for " + uri);
        }
        // every digest family member usable end-to-end with correct digest length
        int[][] len = {{20, 0}, {32, 0}, {48, 0}, {64, 0}, {28, 0}}; // expected byte lengths
        String[] uris = {DigestMethod.SHA1, DigestMethod.SHA256, SHA384, DigestMethod.SHA512, SHA224};
        for (int i = 0; i < uris.length; i++) signAndCheck(sf, uris[i], len[i][0]);
    }

    static void signAndCheck(XMLSignatureFactory sf, String digestUri, int expectBytes) throws Exception {
        KeyPair kp = rsaKP();
        Document doc = newDoc("<r>D</r>");
        DigestMethod dm = sf.newDigestMethod(digestUri, null);
        Reference ref = sf.newReference("", dm,
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        XMLSignature sig = sf.newXMLSignature(si, null);
        sig.sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        byte[] dv = ((Reference) sig.getSignedInfo().getReferences().get(0)).getDigestValue();
        check(dv.length == expectBytes, digestUri + " digest length " + expectBytes + ", got " + dv.length);
    }

    static void testNegatives() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        try { sf.newDigestMethod("http://example.com/no-such-digest", null); check(false, "unregistered digest"); }
        catch (java.security.NoSuchAlgorithmException e) { check(e.getMessage() != null, "NSA for unregistered digest"); }
        try { sf.newDigestMethod(DigestMethod.SHA256, new DigestMethodParameterSpec() {}); check(false, "params rejected"); }
        catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage().contains("no parameters should be specified"), "IAE message for digest params");
        }
    }


    /** JDK-27-only: SHA-224/384 and SHA-3 digest constants and their byte lengths. */
    static void testJdk27Constants() throws Exception {
        check(DigestMethod.SHA224.equals("http://www.w3.org/2001/04/xmldsig-more#sha224"), "SHA224 URI");
        check(DigestMethod.SHA384.equals("http://www.w3.org/2001/04/xmldsig-more#sha384"), "SHA384 URI");
        check(DigestMethod.SHA3_224.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-224"), "SHA3_224 URI");
        check(DigestMethod.SHA3_256.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-256"), "SHA3_256 URI");
        check(DigestMethod.SHA3_384.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-384"), "SHA3_384 URI");
        check(DigestMethod.SHA3_512.equals("http://www.w3.org/2007/05/xmldsig-more#sha3-512"), "SHA3_512 URI");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        java.security.KeyPair kp = kpg.generateKeyPair();
        String[][] cases = {
            {DigestMethod.SHA224, "28"},
            {DigestMethod.SHA384, "48"},
            {DigestMethod.SHA3_224, "28"},
            {DigestMethod.SHA3_256, "32"},
            {DigestMethod.SHA3_384, "48"},
            {DigestMethod.SHA3_512, "64"},
        };
        for (String[] c : cases) {
            DigestMethod dm = sf.newDigestMethod(c[0], null);
            check(dm.getParameterSpec() == null, c[0] + " no params");
            javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
                sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                    (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
                sf.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA256, null),
                java.util.Collections.singletonList(sf.newReference("#d", dm)));
            org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
            org.w3c.dom.Element root = d.createElement("r");
            d.appendChild(root);
            sf.newXMLSignature(si, null,
                java.util.Collections.singletonList(sf.newXMLObject(null, "d", null, null)), null, null)
                .sign(new javax.xml.crypto.dsig.dom.DOMSignContext(kp.getPrivate(), root));
            org.w3c.dom.Element sigEl = (org.w3c.dom.Element)
                root.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature").item(0);
            org.w3c.dom.Element dv = (org.w3c.dom.Element)
                sigEl.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "DigestValue").item(0);
            int len = java.util.Base64.getDecoder().decode(dv.getTextContent().replaceAll("\\s", "")).length;
            check(len == Integer.parseInt(c[1]), "digest length " + c[1] + " for " + c[0] + ", got " + len);
            javax.xml.crypto.dsig.dom.DOMValidateContext vc = new javax.xml.crypto.dsig.dom.DOMValidateContext(
                javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
            check(sf.unmarshalXMLSignature(vc).validate(vc), c[0] + " round-trip validates");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstantsAndInstances();
        testNegatives();
        testJdk27Constants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
