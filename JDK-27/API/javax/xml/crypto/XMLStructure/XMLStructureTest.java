/*
 * @test
 * @summary javax.xml.crypto.XMLStructure conformance.
 * @run main XMLStructureTest
 */

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XMLStructureTest {
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.XMLStructure");
        check(c.isInterface(), "is interface");
        check(c.getInterfaces().length == 0, "extends nothing");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isFeatureSupported"), "declares isFeatureSupported");

        // implementations spanning the dsig object graph
        List<XMLStructure> structures = new ArrayList<XMLStructure>();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        KeyInfoFactory kif = sf.getKeyInfoFactory();
        structures.add(kif.newKeyName("n"));
        structures.add(kif.newKeyValue(java.security.KeyPairGenerator.getInstance("RSA").genKeyPair().getPublic()));
        structures.add(new DOMStructure(newDoc("<r/>").getDocumentElement()));
        javax.xml.crypto.dsig.XMLObject xo = sf.newXMLObject(null, "o", null, null);
        structures.add(xo);
        structures.add(sf.newManifest(Collections.singletonList(
            sf.newReference("#o", sf.newDigestMethod(DigestMethod.SHA1, null)))));
        structures.add(sf.newSignatureProperties(Collections.singletonList(
            sf.newSignatureProperty(Collections.<javax.xml.crypto.XMLStructure>singletonList(
                new DOMStructure(newDoc("<c/>").getDocumentElement())), "#t", "p")), "sp"));
        // every implementation honors the feature contract
        for (XMLStructure s : structures) {
            try { s.isFeatureSupported(null); check(false, "null feature must throw for " + s.getClass().getName()); }
            catch (NullPointerException npe) { check(true, "isFeatureSupported(null) NPE"); }
            check(!s.isFeatureSupported("any.unknown.feature"), "unknown feature false");
        }
        // structures flow through a real signature round-trip
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        KeyInfo ki = kif.newKeyInfo(Collections.<XMLStructure>singletonList(kif.newKeyName("struct-key")));
        Document doc = newDoc("<r>XS</r>");
        Reference ref = sf.newReference("", sf.newDigestMethod(DigestMethod.SHA256, null),
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, ki, Collections.singletonList(xo), null, null)
            .sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(
            javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature vs = sf.unmarshalXMLSignature(vc);
        check(vs.getKeyInfo() instanceof XMLStructure, "unmarshalled KeyInfo is XMLStructure");
        check(vs.getSignedInfo() instanceof XMLStructure, "unmarshalled SignedInfo is XMLStructure");
        check(vs.getObjects().get(0) instanceof XMLStructure, "unmarshalled XMLObject is XMLStructure");
        check(vs.validate(vc), "round-trip validates");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
