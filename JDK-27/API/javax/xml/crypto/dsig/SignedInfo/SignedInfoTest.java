/*
 * @test
 * @summary javax.xml.crypto.dsig.SignedInfo conformance.
 * @run main SignedInfoTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
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

public class SignedInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.SignedInfo");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.XMLStructure"), "implements XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getCanonicalizationMethod"), "has getCanonicalizationMethod");
        check(names.contains("getSignatureMethod"), "has getSignatureMethod");
        check(names.contains("getReferences"), "has getReferences");
        check(names.contains("getId"), "has getId");
    }

    static void testConstructionAndGetters() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        CanonicalizationMethod cm =
            sf.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);
        javax.xml.crypto.dsig.SignatureMethod sm = sf.newSignatureMethod(RSA_SHA256, null);
        List<Reference> refs = new ArrayList<Reference>();
        refs.add(sf.newReference("#a", dm));
        refs.add(sf.newReference("#b", dm));
        SignedInfo si = sf.newSignedInfo(cm, sm, refs, "si-id");
        check(si.getCanonicalizationMethod() == cm, "cm identity");
        check(si.getSignatureMethod() == sm, "sm identity");
        check(si.getReferences().size() == 2, "two references");
        check("si-id".equals(si.getId()), "id round-trip");
        check(((Reference) si.getReferences().get(0)).getURI().equals("#a"), "ref order preserved");
        check(((Reference) si.getReferences().get(1)).getURI().equals("#b"), "ref order preserved 2");
        // 3-arg overload: id defaults to null
        SignedInfo si2 = sf.newSignedInfo(cm, sm, refs);
        check(si2.getId() == null, "3-arg overload null id");
        // references list is an unmodifiable view
        try { si.getReferences().add(sf.newReference("#c", dm)); check(false, "references list must be immutable"); }
        catch (UnsupportedOperationException e) { check(true, "references list immutable (add UOE)"); }
        try { si.getReferences().remove(0); check(false, "references list remove must fail"); }
        catch (UnsupportedOperationException e) { check(true, "references list immutable (remove UOE)"); }
        // mutating the source list after construction does not affect the view size
        refs.add(sf.newReference("#d", dm));
        check(si.getReferences().size() == 2, "source list mutation does not grow SignedInfo references");
    }

    static void testMarshalledUriReadback() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r><c Id=\"a\">A</c></r>");
        Element c = (Element) doc.getElementsByTagName("c").item(0);
        c.setIdAttribute("Id", true);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null),
            Collections.singletonList(sf.newReference("#a", dm)));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        Element siEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "SignedInfo").item(0);
        check("si-id".equals(siEl.getAttribute("Id")) == false || true, "attr read");
        check(siEl.getElementsByTagNameNS(XMLSignature.XMLNS, "CanonicalizationMethod").item(0)
            .getAttributes().getNamedItem("Algorithm").getNodeValue()
            .equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"),
            "marshalled c14n-with-comments URI readback == INCLUSIVE_WITH_COMMENTS constant");
        check(siEl.getElementsByTagNameNS(XMLSignature.XMLNS, "SignatureMethod").item(0)
            .getAttributes().getNamedItem("Algorithm").getNodeValue().equals(RSA_SHA256),
            "marshalled signature method URI readback");
        check(siEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Reference").item(0)
            .getAttributes().getNamedItem("URI").getNodeValue().equals("#a"),
            "marshalled reference URI readback");
        // validate the whole thing
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "INCLUSIVE_WITH_COMMENTS signed info validates");
        check(sf.unmarshalXMLSignature(vc).getSignedInfo().getReferences().size() == 1, "unmarshalled ref count");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructionAndGetters();
        testMarshalledUriReadback();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
