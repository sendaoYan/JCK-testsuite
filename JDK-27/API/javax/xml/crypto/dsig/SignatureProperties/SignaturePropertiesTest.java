/*
 * @test
 * @summary javax.xml.crypto.dsig.SignatureProperties conformance.
 * @run main SignaturePropertiesTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Collections;

public class SignaturePropertiesTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.SignatureProperties");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true;
        check(xmls, "implements XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getId"), "has getId");
        check(names.contains("getProperties"), "has getProperties");
    }

    static void testGetterMatrix() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document d = newDoc("<r/>");
        SignatureProperty p1 = sf.newSignatureProperty(
            Collections.singletonList((XMLStructure) new DOMStructure(d.createTextNode("v1"))), "#target1", "p1");
        SignatureProperty p2 = sf.newSignatureProperty(
            Collections.singletonList((XMLStructure) new DOMStructure(d.createTextNode("v2"))), "#target2", "p2");
        SignatureProperties sp = sf.newSignatureProperties(new ArrayList<SignatureProperty>(
            java.util.Arrays.asList(p1, p2)), "props-id");
        check("props-id".equals(sp.getId()), "id round-trip");
        check(sp.getProperties().size() == 2, "two properties");
        check(sp.getProperties().get(0) == p1, "property identity/order");
        // null id form
        SignatureProperties sp2 = sf.newSignatureProperties(java.util.Arrays.asList(p1), null);
        check(sp2.getId() == null, "null id");
        try { sp.getProperties().remove(0); check(false, "properties immutable"); }
        catch (UnsupportedOperationException e) { check(true, "properties immutable UOE"); }
        // negatives
        try { sf.newSignatureProperties(null, null); check(false, "null list"); }
        catch (NullPointerException e) { check(true, "null list NPE"); }
        try { sf.newSignatureProperties(new ArrayList<SignatureProperty>(), null); check(false, "empty list"); }
        catch (IllegalArgumentException e) { check(true, "empty list IAE"); }
    }

    /** SignatureProperties embedded in a signed object round-trips through marshal/unmarshal. */
    static void testEmbeddedRoundTrip() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r/>");
        Document aux = newDoc("<sigMeta xmlns=\"urn:meta\">info</sigMeta>");
        SignatureProperty prop = sf.newSignatureProperty(
            Collections.singletonList((XMLStructure) new DOMStructure(aux.getDocumentElement())), "#the-signature", "sp0");
        SignatureProperties props = sf.newSignatureProperties(Collections.singletonList(prop), "props0");
        XMLObject xo = sf.newXMLObject(Collections.singletonList((XMLStructure) props), "po", null, null);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null),
            Collections.singletonList(sf.newReference("#po", dm)));
        sf.newXMLSignature(si, null, Collections.singletonList(xo), null, null)
            .sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "signature with embedded SignatureProperties validates");
        XMLObject vxo = (XMLObject) v.getObjects().get(0);
        SignatureProperties vp = (SignatureProperties) vxo.getContent().get(0);
        check("props0".equals(vp.getId()), "unmarshalled properties id");
        check(vp.getProperties().size() == 1, "unmarshalled property count");
        SignatureProperty vprop = (SignatureProperty) vp.getProperties().get(0);
        check("sp0".equals(vprop.getId()), "unmarshalled property id");
        check(vprop.getTarget().equals("#the-signature"), "unmarshalled property target");
        check(vprop.getContent().size() == 1, "unmarshalled property content size");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetterMatrix();
        testEmbeddedRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
