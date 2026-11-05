/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.KeyInfo conformance.
 * @run main KeyInfoTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Collections;

public class KeyInfoTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.KeyInfo");
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
        check(names.contains("marshal"), "has marshal");
    }

    static void testContentSemantics() throws Exception {
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        KeyPair kp = rsaKP();
        KeyName kn = kif.newKeyName("name-1");
        KeyValue kv = kif.newKeyValue(kp.getPublic());
        KeyInfo ki = kif.newKeyInfo(Arrays.asList(kn, kv));
        check(ki.getId() == null, "id null default");
        check(ki.getContent().size() == 2, "content size");
        // XMLStructure enumeration with instanceof dispatch
        int keyNames = 0, keyValues = 0;
        for (Object o : ki.getContent()) {
            if (o instanceof KeyName) keyNames++;
            if (o instanceof KeyValue) keyValues++;
        }
        check(keyNames == 1 && keyValues == 1, "instanceof dispatch over content");
        // immutable content
        try { ki.getContent().remove(0); check(false, "content immutable"); }
        catch (UnsupportedOperationException e) { check(true, "content immutable UOE"); }
        // isFeatureSupported contract on the concrete KeyInfo instance
        try { ki.isFeatureSupported(null); check(false, "null feature NPE"); }
        catch (NullPointerException e) { check(true, "isFeatureSupported(null) NPE"); }
        check(!ki.isFeatureSupported("any"), "unknown feature false");
    }

    /** Two key delivery channels: KeySelector callback receives the KeyInfo; KeyValue inside KeyInfo resolves the key. */
    static void testKeyDeliveryDualChannel() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        KeyInfoFactory kif = sf.getKeyInfoFactory();
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kif.newKeyValue(kp.getPublic())));
        Document doc = newDoc("<r>K</r>");
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null),
            Collections.singletonList(sf.newReference("", dm,
                Collections.singletonList(sf.newTransform(javax.xml.crypto.dsig.Transform.ENVELOPED,
                    (javax.xml.crypto.dsig.spec.TransformParameterSpec) null)), null, null)));
        sf.newXMLSignature(si, ki, null, null, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        // channel 1: selector receives the KeyInfo object itself
        final KeyInfo[] seen = new KeyInfo[1];
        DOMValidateContext vc = new DOMValidateContext(new javax.xml.crypto.KeySelector() {
            public javax.xml.crypto.KeySelectorResult select(KeyInfo k, javax.xml.crypto.KeySelector.Purpose p,
                    javax.xml.crypto.AlgorithmMethod m, javax.xml.crypto.XMLCryptoContext c)
                    throws javax.xml.crypto.KeySelectorException {
                seen[0] = k;
                return KeySelector.singletonKeySelector(kp.getPublic()).select(k, p, m, c);
            }
        }, sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "selector channel validates");
        check(seen[0] != null && seen[0].getContent().size() == 1
            && seen[0].getContent().get(0) instanceof KeyValue, "selector callback receives unmarshalled KeyInfo with KeyValue");
        // channel 2: context-level fixed key, KeyInfo ignored
        DOMValidateContext vc2 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(vc2).validate(vc2), "context-key channel validates");
    }

    /** KeyInfo.marshal writes the KeyInfo element into an arbitrary parent (standalone marshal). */
    static void testStandaloneMarshal() throws Exception {
        KeyPair kp = rsaKP();
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        Document d = newDoc("<r/>");
        KeyInfo ki = kif.newKeyInfo(Arrays.asList(kif.newKeyName("mn"), kif.newKeyValue(kp.getPublic())), "mk");
        Element parent = d.createElementNS("urn:h", "holder");
        ki.marshal(new DOMStructure(parent), new DOMSignContext(kp.getPrivate(), d.getDocumentElement()));
        check(parent.getChildNodes().getLength() == 1, "marshal appended one child");
        check(parent.getFirstChild().getLocalName().equals("KeyInfo"), "marshalled child is KeyInfo");
        check(parent.getFirstChild().getNamespaceURI().equals(XMLSignature.XMLNS), "KeyInfo in dsig namespace");
        // round-trip: unmarshal from the standalone element
        KeyInfo back = kif.unmarshalKeyInfo(new DOMStructure((Element) parent.getFirstChild()));
        check("mk".equals(back.getId()), "standalone round-trip id");
        check(back.getContent().size() == 2, "standalone round-trip content size");
        check(((KeyName) back.getContent().get(0)).getName().equals("mn"), "standalone round-trip KeyName");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testContentSemantics();
        testKeyDeliveryDualChannel();
        testStandaloneMarshal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
