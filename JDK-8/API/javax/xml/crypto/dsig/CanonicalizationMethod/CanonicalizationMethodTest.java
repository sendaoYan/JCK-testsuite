/*
 * @test
 * @summary javax.xml.crypto.dsig.CanonicalizationMethod conformance.
 * @run main CanonicalizationMethodTest
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
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Collections;

public class CanonicalizationMethodTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.CanonicalizationMethod");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean tr = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.dsig.Transform")) tr = true;
        check(tr, "extends Transform");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getParameterSpec"), "has getParameterSpec");
    }

    static void testConstantsAndInstantiation() throws Exception {
        check(CanonicalizationMethod.INCLUSIVE.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315"), "INCLUSIVE");
        check(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"), "INCLUSIVE_WITH_COMMENTS");
        check(CanonicalizationMethod.EXCLUSIVE.equals("http://www.w3.org/2001/10/xml-exc-c14n#"), "EXCLUSIVE");
        check(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS.equals("http://www.w3.org/2001/10/xml-exc-c14n#WithComments"), "EXCLUSIVE_WITH_COMMENTS");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        for (String uri : new String[] {CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                CanonicalizationMethod.EXCLUSIVE, CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS}) {
            CanonicalizationMethod cm = sf.newCanonicalizationMethod(uri, (C14NMethodParameterSpec) null);
            check(cm.getAlgorithm().equals(uri), "getAlgorithm round-trip " + uri);
            check(cm.getParameterSpec() == null, "null spec for " + uri);
        }
        // EXCLUSIVE accepts ExcC14NParameterSpec; spec round-trips
        ExcC14NParameterSpec ps = new ExcC14NParameterSpec(Arrays.asList("p1", "p2"));
        CanonicalizationMethod ex = sf.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, ps);
        check(ex.getParameterSpec() == ps, "ExcC14NParameterSpec identity round-trip");
        // negatives
        try { sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, ps); check(false, "inclusive with spec"); }
        catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage().contains("no parameters should be specified"), "inclusive rejects spec params");
        }
        try { sf.newCanonicalizationMethod("http://example.com/c14n-x", (C14NMethodParameterSpec) null); check(false, "unregistered c14n"); }
        catch (java.security.NoSuchAlgorithmException e) { check(true, "unregistered c14n NSA"); }
    }

    /**
     * Canonicalization behavior difference: an EXC_C14N transform applied to an element under
     * unused namespace declarations digests differently than INCLUSIVE (which pulls in ancestor
     * namespaces); an explicit PrefixList changes the exclusive digest again.
     */
    static void testExclusiveVsInclusiveDigestDifference() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        byte[] inc = digestWithTransform(sf, CanonicalizationMethod.INCLUSIVE, null);
        byte[] exc = digestWithTransform(sf, CanonicalizationMethod.EXCLUSIVE, null);
        check(!Arrays.equals(inc, exc), "INCLUSIVE vs EXCLUSIVE digests differ under unused namespaces");
        byte[] excPfx = digestWithTransform(sf, CanonicalizationMethod.EXCLUSIVE,
            new ExcC14NParameterSpec(Arrays.asList("pfx1")));
        check(!Arrays.equals(exc, excPfx), "explicit PrefixList changes EXCLUSIVE digest");
        // all variants still validate end-to-end (self-consistent sign+validate)
        Document d = newDoc("<r xmlns:pfx1=\"urn:one\" xmlns:unused=\"urn:two\"><t Id=\"t1\">V</t></r>");
        Element t = (Element) d.getElementsByTagName("t").item(0); t.setIdAttribute("Id", true);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("#t1", dm,
            Collections.singletonList(sf.newTransform(CanonicalizationMethod.INCLUSIVE, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), d.getDocumentElement()));
        Element sigEl = (Element) d.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        javax.xml.crypto.dsig.dom.DOMValidateContext vc =
            new javax.xml.crypto.dsig.dom.DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "c14n-transformed reference validates end-to-end");
    }

    static byte[] digestWithTransform(XMLSignatureFactory sf, String c14n, TransformParameterSpec spec) throws Exception {
        KeyPair kp = rsaKP();
        Document d = newDoc("<r xmlns:pfx1=\"urn:one\" xmlns:unused=\"urn:two\"><t Id=\"t1\">V</t></r>");
        Element t = (Element) d.getElementsByTagName("t").item(0); t.setIdAttribute("Id", true);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("#t1", dm, Collections.singletonList(sf.newTransform(c14n, spec)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        XMLSignature s = sf.newXMLSignature(si, null);
        s.sign(new DOMSignContext(kp.getPrivate(), d.getDocumentElement()));
        return ((Reference) s.getSignedInfo().getReferences().get(0)).getDigestValue();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstantsAndInstantiation();
        testExclusiveVsInclusiveDigestDifference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
