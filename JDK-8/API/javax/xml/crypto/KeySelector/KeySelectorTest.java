/*
 * @test
 * @summary javax.xml.crypto.KeySelector conformance: abstract class hierarchy and methods.
 * @run main KeySelectorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
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
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeySelectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    /** Records everything the framework hands to a selector during validation. */
    static class RecordingSelector extends KeySelector {
        List<KeyInfo> keyInfos = new ArrayList<KeyInfo>();
        List<KeySelector.Purpose> purposes = new ArrayList<KeySelector.Purpose>();
        List<AlgorithmMethod> methods = new ArrayList<AlgorithmMethod>();
        List<XMLCryptoContext> contexts = new ArrayList<XMLCryptoContext>();
        int calls;
        final PublicKey key;
        RecordingSelector(PublicKey key) { this.key = key; }
        public KeySelectorResult select(KeyInfo k, KeySelector.Purpose p, AlgorithmMethod m, XMLCryptoContext c)
                throws KeySelectorException {
            calls++;
            keyInfos.add(k);
            purposes.add(p);
            methods.add(m);
            contexts.add(c);
            return new KeySelectorResult() {
                public java.security.Key getKey() { return key; }
            };
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.KeySelector");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("select"), "has method select");
        check(names.contains("singletonKeySelector"), "has method singletonKeySelector");

        // --- singletonKeySelector: null rejected, delivered key returned verbatim ---
        try { KeySelector t = KeySelector.singletonKeySelector(null); check(false, "null should throw: " + t); }
        catch (NullPointerException e) { check(true, "singletonKeySelector(null) NPE"); }
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        KeySelector singleton = KeySelector.singletonKeySelector(kp.getPublic());
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.keyinfo.KeyInfoFactory kif = sf.getKeyInfoFactory();
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kif.newKeyName("ignored")));
        KeySelectorResult res = singleton.select(ki, KeySelector.Purpose.VERIFY,
            sf.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null), null);
        check(res != null && res.getKey() == kp.getPublic(), "singleton returns the fixed key");
        check(singleton.select(ki, KeySelector.Purpose.SIGN,
            sf.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null), null).getKey() == kp.getPublic(),
            "singleton ignores purpose");

        // --- callback parameter matrix during a real validation ---
        Document doc = newDoc("<r>KS</r>");
        KeyInfoFactory kif2 = sf.getKeyInfoFactory();
        KeyInfo realKi = kif2.newKeyInfo(Collections.<javax.xml.crypto.XMLStructure>singletonList(kif2.newKeyName("selector-key")));
        Reference ref = sf.newReference("", sf.newDigestMethod(DigestMethod.SHA256, null),
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, realKi, null, null, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        RecordingSelector recorder = new RecordingSelector(kp.getPublic());
        DOMValidateContext vc = new DOMValidateContext(recorder, sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "recording selector validates");
        check(recorder.calls == 1, "selector invoked exactly once per validation");
        KeyInfo seenKi = recorder.keyInfos.get(0);
        check(seenKi != null && seenKi.getContent().size() == 1 && seenKi.getContent().get(0) instanceof KeyName
            && "selector-key".equals(((KeyName) seenKi.getContent().get(0)).getName()),
            "callback receives the unmarshalled KeyInfo content");
        check(recorder.purposes.get(0) == KeySelector.Purpose.VERIFY, "callback purpose is VERIFY");
        check(RSA_SHA256.equals(recorder.methods.get(0).getAlgorithm()),
            "callback AlgorithmMethod carries the signature method URI");
        check(recorder.methods.get(0) instanceof javax.xml.crypto.dsig.SignatureMethod, "callback method is a SignatureMethod");
        check(recorder.contexts.get(0) == vc, "callback receives the validate context by identity");

        // --- selector returning null => XMLSignatureException with the precise message ---
        DOMValidateContext vn = new DOMValidateContext(new KeySelector() {
            public KeySelectorResult select(KeyInfo k, KeySelector.Purpose p, AlgorithmMethod m, XMLCryptoContext x) {
                return null;
            }
        }, sigEl);
        try {
            sf.unmarshalXMLSignature(vn).validate(vn);
            check(false, "null-result selector should fail validation");
        } catch (javax.xml.crypto.dsig.XMLSignatureException e) {
            check(e.getMessage().contains("keyselector did not find"), "null-result message: " + e.getMessage());
        }

        // --- selector throwing KeySelectorException => wrapped, cause preserved ---
        final KeySelectorException boom = new KeySelectorException("no key available");
        DOMValidateContext vt = new DOMValidateContext(new KeySelector() {
            public KeySelectorResult select(KeyInfo k, KeySelector.Purpose p, AlgorithmMethod m, XMLCryptoContext x)
                    throws KeySelectorException { throw boom; }
        }, sigEl);
        try {
            sf.unmarshalXMLSignature(vt).validate(vt);
            check(false, "throwing selector should fail validation");
        } catch (javax.xml.crypto.dsig.XMLSignatureException e) {
            check(e.getCause() == boom, "selector exception preserved as cause");
            check(e.getMessage().contains("cannot find validation key"), "wrapping message");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
