/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.HMACParameterSpec conformance.
 * @run main HMACParameterSpecTest
 */

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
import javax.xml.crypto.dsig.spec.HMACParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class HMACParameterSpecTest {
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

    static final String HMAC_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#hmac-sha256";

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.HMACParameterSpec");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean sm = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec")) sm = true;
        check(sm, "implements SignatureMethodParameterSpec");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOutputLength"), "has getOutputLength");
    }

    static void testConstructorMatrix() {
        check(new HMACParameterSpec(256).getOutputLength() == 256, "outputLength 256");
        check(new HMACParameterSpec(0).getOutputLength() == 0, "zero outputLength constructible");
        check(new HMACParameterSpec(1).getOutputLength() == 1, "one outputLength constructible");
        check(new HMACParameterSpec(Integer.MAX_VALUE).getOutputLength() == Integer.MAX_VALUE, "max outputLength constructible");
        // marker interface assignability
        SignatureMethodParameterSpec asSpec = new HMACParameterSpec(256);
        check(asSpec instanceof javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec, "assignable to SignatureMethodParameterSpec");
    }

    /** Signature value length follows outputLength/8; below-minimum length rejected at sign time. */
    static void testOutputLengthBehavior() throws Exception {
        SecretKey hk = KeyGenerator.getInstance("HmacSHA256").generateKey();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        for (int bits : new int[] {256}) {
            Document doc = newDoc("<r>H</r>");
            DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
            Reference ref = sf.newReference("", dm,
                Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
            SignedInfo si = sf.newSignedInfo(
                sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                sf.newSignatureMethod(HMAC_SHA256, new HMACParameterSpec(bits)),
                Collections.singletonList(ref));
            XMLSignature sig = sf.newXMLSignature(si, null);
            sig.sign(new DOMSignContext(hk, doc.getDocumentElement()));
            check(sig.getSignatureValue().getValue().length == bits / 8,
                "signature value length == outputLength/8 (" + bits + " bits)");
            Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
            Element smEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "SignatureMethod").item(0);
            Element pEl = (Element) smEl.getElementsByTagNameNS(XMLSignature.XMLNS, "HMACOutputLength").item(0);
            check(pEl != null && pEl.getTextContent().equals(String.valueOf(bits)),
                "HMACOutputLength parameter marshalled as " + bits);
            DOMValidateContext vc = new DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(hk), sigEl);
            XMLSignature v = sf.unmarshalXMLSignature(vc);
            check(v.validate(vc), "explicit output-length hmac validates");
            javax.xml.crypto.dsig.SignatureMethod vsm = v.getSignedInfo().getSignatureMethod();
            check(vsm.getParameterSpec() instanceof HMACParameterSpec, "unmarshalled method carries HMACParameterSpec");
            check(((HMACParameterSpec) vsm.getParameterSpec()).getOutputLength() == bits,
                "unmarshalled HMACParameterSpec outputLength round-trip");
        }
        // below the HMAC's own digest length: sign() must reject
        Document doc2 = newDoc("<r>H</r>");
        DigestMethod dm2 = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref2 = sf.newReference("", dm2,
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si2 = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(HMAC_SHA256, new HMACParameterSpec(128)),
            Collections.singletonList(ref2));
        try {
            sf.newXMLSignature(si2, null).sign(new DOMSignContext(hk, doc2.getDocumentElement()));
            check(false, "HMACOutputLength below digest length must fail");
        } catch (javax.xml.crypto.dsig.XMLSignatureException e) {
            check(e.getMessage().contains("HMACOutputLength"), "rejection message mentions HMACOutputLength: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructorMatrix();
        testOutputLengthBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
