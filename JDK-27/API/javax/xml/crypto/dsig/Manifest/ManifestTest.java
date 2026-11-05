/*
 * @test
 * @summary javax.xml.crypto.dsig.Manifest conformance.
 * @run main ManifestTest
 */

import javax.xml.crypto.KeySelector;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
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
import java.util.List;

public class ManifestTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.Manifest");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true;
        check(xmls, "implements XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getId"), "has getId");
        check(names.contains("getReferences"), "has getReferences");
    }

    static void testGetterMatrix() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        List<Reference> refs = new ArrayList<Reference>();
        refs.add(sf.newReference("#m1", dm));
        refs.add(sf.newReference("#m2", dm));
        refs.add(sf.newReference("#m3", dm));
        Manifest man = sf.newManifest(refs, "manifest-id");
        check("manifest-id".equals(man.getId()), "id round-trip");
        check(man.getReferences().size() == 3, "three references");
        check(((Reference) man.getReferences().get(1)).getURI().equals("#m2"), "reference order");
        // id-less form
        Manifest man2 = sf.newManifest(refs, null);
        check(man2.getId() == null, "null id round-trip");
        try { man.getReferences().clear(); check(false, "manifest references immutable"); }
        catch (UnsupportedOperationException e) { check(true, "manifest references immutable UOE"); }
        // mutating the source list afterwards does not change the manifest view
        refs.add(sf.newReference("#m4", dm));
        check(man.getReferences().size() == 3, "source list mutation isolated");
        // negatives
        try { sf.newManifest(null, null); check(false, "null refs"); }
        catch (NullPointerException e) { check(true, "null refs NPE"); }
        try { sf.newManifest(new ArrayList<Reference>(), null); check(false, "empty refs"); }
        catch (IllegalArgumentException e) { check(true, "empty refs IAE"); }
    }

    /**
     * Manifest inside an XMLObject: outer signature covers the Manifest itself, the Manifest's
     * own references validate independently and are NOT part of core validation.
     */
    static void testEmbeddedManifestSemantics() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r><a Id=\"m1\">A</a><b Id=\"m2\">B</b></r>");
        Element ea = (Element) doc.getElementsByTagName("a").item(0); ea.setIdAttribute("Id", true);
        Element eb = (Element) doc.getElementsByTagName("b").item(0); eb.setIdAttribute("Id", true);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        List<Reference> mrefs = new ArrayList<Reference>();
        mrefs.add(sf.newReference("#m1", dm));
        mrefs.add(sf.newReference("#m2", dm));
        Manifest man = sf.newManifest(mrefs, "mid");
        XMLObject xo = sf.newXMLObject(Collections.singletonList((XMLStructure) man), "mo", null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null),
            Collections.singletonList(sf.newReference("#mo", dm)));
        sf.newXMLSignature(si, null, Collections.singletonList(xo), null, null)
            .sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "outer signature over manifest object validates");
        // inner manifest references all validate when explicitly validated
        XMLObject vxo = (XMLObject) v.getObjects().get(0);
        Manifest vman = (Manifest) vxo.getContent().get(0);
        check("mid".equals(vman.getId()), "unmarshalled manifest id");
        check(vman.getReferences().size() == 2, "unmarshalled manifest ref count");
        boolean all = true;
        for (Object o : vman.getReferences()) all &= ((Reference) o).validate(vc);
        check(all, "inner manifest references validate before tamper");
        // tamper m1: outer Reference #mo digest changes (manifest bytes changed)...
        ea.setTextContent("HACK");
        DOMValidateContext vc2 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v2 = sf.unmarshalXMLSignature(vc2);
        Reference im1 = (Reference) ((Manifest) ((XMLObject) v2.getObjects().get(0)).getContent().get(0)).getReferences().get(0);
        Reference im2 = (Reference) ((Manifest) ((XMLObject) v2.getObjects().get(0)).getContent().get(0)).getReferences().get(1);
        check(!im1.validate(vc2), "tampered m1 manifest reference invalid");
        check(im2.validate(vc2), "untampered m2 manifest reference still valid");
        // tampering content under the manifest does NOT invalidate the outer core signature
        check(v2.validate(vc2), "outer core validation unaffected by manifest-content tamper (manifest refs not core-validated)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetterMatrix();
        testEmbeddedManifestSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
