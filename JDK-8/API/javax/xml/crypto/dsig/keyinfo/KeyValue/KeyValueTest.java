/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.KeyValue conformance.
 * @run main KeyValueTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;

public class KeyValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    static final String ECDSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.KeyValue");
        check(c.isInterface(), "is interface");
        check(KeyValue.RSA_TYPE.equals("http://www.w3.org/2000/09/xmldsig#RSAKeyValue"), "RSA_TYPE constant");
        check(KeyValue.DSA_TYPE.equals("http://www.w3.org/2000/09/xmldsig#DSAKeyValue"), "DSA_TYPE constant");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPublicKey"), "has getPublicKey");
    }

    static void testKeyAlgorithms() throws Exception {
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        // RSA
        KeyPairGenerator rpg = KeyPairGenerator.getInstance("RSA");
        rpg.initialize(2048);
        KeyPair rk = rpg.generateKeyPair();
        KeyValue rkv = kif.newKeyValue(rk.getPublic());
        check(rkv.getPublicKey() instanceof RSAPublicKey, "RSA KeyValue yields RSAPublicKey");
        check(rkv.getPublicKey().equals(rk.getPublic()), "RSA public key round-trip equality");
        // DSA
        KeyPairGenerator dpg = KeyPairGenerator.getInstance("DSA");
        dpg.initialize(1024);
        KeyPair dk = dpg.generateKeyPair();
        KeyValue dkv = kif.newKeyValue(dk.getPublic());
        check(dkv.getPublicKey() instanceof DSAPublicKey, "DSA KeyValue yields DSAPublicKey");
        check(dkv.getPublicKey().equals(dk.getPublic()), "DSA public key round-trip");
        check(dkv.getPublicKey().getAlgorithm().equals("DSA"), "DSA algorithm name");
        // EC
        KeyPairGenerator epg = KeyPairGenerator.getInstance("EC");
        epg.initialize(256);
        KeyPair ek = epg.generateKeyPair();
        KeyValue ekv = kif.newKeyValue(ek.getPublic());
        check(ekv.getPublicKey() instanceof ECPublicKey, "EC KeyValue yields ECPublicKey");
        check(ekv.getPublicKey().equals(ek.getPublic()), "EC public key round-trip");
        // null -> NPE
        try { kif.newKeyValue(null); check(false, "null key"); }
        catch (NullPointerException e) { check(true, "newKeyValue(null) NPE"); }
        // unsupported algorithm -> KeyException naming the algorithm
        try {
            KeyPairGenerator dhg = KeyPairGenerator.getInstance("DiffieHellman");
            kif.newKeyValue(dhg.generateKeyPair().getPublic());
            check(false, "DH rejected");
        } catch (java.security.KeyException e) {
            check(e.getMessage().contains("DH"), "KeyException message names DH");
        }
    }

    /** KeyValue marshal/unmarshal inside a real RSA signature; validation via KeyValue channel. */
    static void testMarshalAndValidationChannel() throws Exception {
        KeyPairGenerator rpg = KeyPairGenerator.getInstance("RSA");
        rpg.initialize(2048);
        KeyPair kp = rpg.generateKeyPair();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        KeyInfoFactory kif = sf.getKeyInfoFactory();
        Document doc = newDoc("<r>V</r>");
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kif.newKeyValue(kp.getPublic())));
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null),
            Collections.singletonList(sf.newReference("", dm,
                Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null)));
        sf.newXMLSignature(si, ki, null, null, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        // marshalled RSAKeyValue carries modulus/exponent
        Element rsaEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "RSAKeyValue").item(0);
        check(rsaEl != null, "RSAKeyValue element marshalled");
        check(rsaEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Modulus").getLength() == 1, "Modulus child");
        check(rsaEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Exponent").getLength() == 1, "Exponent child");
        // unmarshalled KeyValue resolves to an equal key; validation succeeds through it
        KeyInfo back = kif.unmarshalKeyInfo(new DOMStructure(
            (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo").item(0)));
        KeyValue v = (KeyValue) back.getContent().get(0);
        check(v.getPublicKey().equals(kp.getPublic()), "unmarshalled KeyValue equals original key");
        DOMValidateContext vc = new DOMValidateContext(new KeyValueSelector(), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "KeyValue-channel validation succeeds");
    }

    /** ECDSA signature with EC KeyValue in KeyInfo: full round-trip on P-256. */
    static void testEcRoundTrip() throws Exception {
        KeyPairGenerator epg = KeyPairGenerator.getInstance("EC");
        epg.initialize(256);
        KeyPair ek = epg.generateKeyPair();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        KeyInfoFactory kif = sf.getKeyInfoFactory();
        Document doc = newDoc("<r>E</r>");
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kif.newKeyValue(ek.getPublic())));
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(ECDSA_SHA256, null),
            Collections.singletonList(sf.newReference("", dm,
                Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null)));
        sf.newXMLSignature(si, ki, null, null, null).sign(new DOMSignContext(ek.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(ek.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "ECDSA-SHA256 round-trip validates");
        // ECDSA-SHA256 signature is DER-encoded; length varies but bounded
        int len = sf.unmarshalXMLSignature(vc).getSignatureValue().getValue().length;
        check(len >= 64 && len <= 80, "ECDSA P-256 DER signature bounded (64..80), got " + len);
    }

    static class KeyValueSelector extends javax.xml.crypto.KeySelector {
        public javax.xml.crypto.KeySelectorResult select(KeyInfo k, javax.xml.crypto.KeySelector.Purpose p,
                javax.xml.crypto.AlgorithmMethod m, javax.xml.crypto.XMLCryptoContext c) throws javax.xml.crypto.KeySelectorException {
            if (k != null) for (Object o : k.getContent()) {
                if (o instanceof KeyValue) {
                    try { return KeySelector.singletonKeySelector(((KeyValue) o).getPublicKey()).select(k, p, m, c); }
                    catch (java.security.KeyException e) { throw new javax.xml.crypto.KeySelectorException(e); }
                }
            }
            throw new javax.xml.crypto.KeySelectorException("no KeyValue");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testKeyAlgorithms();
        testMarshalAndValidationChannel();
        testEcRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
