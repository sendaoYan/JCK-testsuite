/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.KeyName conformance.
 * @run main KeyNameTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
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
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Collections;

public class KeyNameTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.KeyName");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true;
        check(xmls, "implements XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "has getName");
    }

    static void testSemantics() throws Exception {
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        KeyName kn = kif.newKeyName("the-name");
        check(kn.getName().equals("the-name"), "name round-trip");
        // empty string legal
        check(kif.newKeyName("").getName().equals(""), "empty name accepted");
        // null rejected with message
        try { kif.newKeyName(null); check(false, "null name"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("name"), "null name NPE message"); }
        // feature support contract
        try { kn.isFeatureSupported(null); check(false, "null feature"); }
        catch (NullPointerException e) { check(true, "isFeatureSupported(null) NPE"); }
        check(!kn.isFeatureSupported("whatever"), "unknown feature false");
    }

    static void testMarshalRoundTrip() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        KeyInfoFactory kif = sf.getKeyInfoFactory();
        Document doc = newDoc("<r/>");
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kif.newKeyName("marshalled-name")));
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null),
            Collections.singletonList(sf.newReference("", dm,
                Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null)));
        sf.newXMLSignature(si, ki, null, null, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        Element kiEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo").item(0);
        Element knEl = (Element) kiEl.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyName").item(0);
        check(knEl.getTextContent().equals("marshalled-name"), "marshalled KeyName text");
        // unmarshal round-trip
        KeyInfo back = kif.unmarshalKeyInfo(new DOMStructure(kiEl));
        KeyName vkn = (KeyName) back.getContent().get(0);
        check(vkn.getName().equals("marshalled-name"), "unmarshalled KeyName value round-trip");
        // validation still passes with selector keyed on the name
        final java.security.PublicKey pub = kp.getPublic();
        DOMValidateContext vc = new DOMValidateContext(new javax.xml.crypto.KeySelector() {
            public javax.xml.crypto.KeySelectorResult select(KeyInfo k, javax.xml.crypto.KeySelector.Purpose p,
                    javax.xml.crypto.AlgorithmMethod m, javax.xml.crypto.XMLCryptoContext c) throws javax.xml.crypto.KeySelectorException {
                for (Object o : k.getContent()) {
                    if (o instanceof KeyName && "marshalled-name".equals(((KeyName) o).getName())) {
                        return KeySelector.singletonKeySelector(pub).select(k, p, m, c);
                    }
                }
                throw new javax.xml.crypto.KeySelectorException("unknown key name");
            }
        }, sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "name-based selector validates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSemantics();
        testMarshalRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
