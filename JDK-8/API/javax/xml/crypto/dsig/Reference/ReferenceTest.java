/*
 * @test
 * @summary javax.xml.crypto.dsig.Reference conformance.
 * @run main ReferenceTest
 */

import javax.xml.crypto.Data;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
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
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReferenceTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.Reference");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.URIReference"), "implements URIReference");
        check(has(ifs, "javax.xml.crypto.XMLStructure"), "implements XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"getTransforms", "getDigestMethod", "getId", "getDigestValue",
                "getCalculatedDigestValue", "validate", "getDereferencedData", "getDigestInputStream"})
            check(names.contains(n), "has method " + n);
    }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    static void testGetterMatrix() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        // minimal form
        Reference r = sf.newReference("#x", dm);
        check("#x".equals(r.getURI()), "uri round-trip");
        check(r.getType() == null, "type null when not set");
        check(r.getId() == null, "id null when not set");
        check(r.getTransforms() == null || r.getTransforms().isEmpty(), "no transforms");
        check(r.getDigestMethod() == dm, "digest method identity");
        check(r.getDigestValue() == null, "digest value null before sign");
        check(r.getCalculatedDigestValue() == null, "calculated digest null before validation");
        // full form
        List<Transform> ts = new ArrayList<Transform>();
        ts.add(sf.newTransform(Transform.BASE64, (TransformParameterSpec) null));
        Reference r2 = sf.newReference("#y", dm, ts, "http://www.w3.org/2000/09/xmldsig#Object", "rid");
        check("#y".equals(r2.getURI()), "uri full form");
        check("http://www.w3.org/2000/09/xmldsig#Object".equals(r2.getType()), "type round-trip");
        check("rid".equals(r2.getId()), "id round-trip");
        check(r2.getTransforms().size() == 1, "one transform");
        check(((Transform) r2.getTransforms().get(0)).getAlgorithm().equals(Transform.BASE64), "transform algorithm");
        // supplied digestValue variant
        byte[] dv = new byte[32];
        Reference r3 = sf.newReference("#z", dm, null, null, null, dv);
        check(r3.getDigestValue() != null && r3.getDigestValue().length == 32, "supplied digest value stored");
        check(r3.getDigestValue() != dv, "supplied digest value defensively copied");
        // nulls
        // null URI is accepted (a Reference may have no URI); getter returns null
        Reference rn = sf.newReference(null, dm);
        check(rn.getURI() == null, "null uri accepted, getURI null");
        try { sf.newReference("#x", null); check(false, "null dm"); }
        catch (NullPointerException e) { check(true, "null dm NPE"); }
        // empty reference list rejected at SignedInfo level, not Reference
        check(r.getDigestInputStream() == null, "digest input stream null outside validation");
        check(r.getDereferencedData() == null, "dereferenced data null outside validation");
    }

    /** Digest computed at sign time; per-reference validation granularity after tamper. */
    static void testDigestComputationAndGranularity() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r><a Id=\"e1\">ONE</a><b Id=\"e2\">TWO</b></r>");
        Element ea = (Element) doc.getElementsByTagName("a").item(0); ea.setIdAttribute("Id", true);
        Element eb = (Element) doc.getElementsByTagName("b").item(0); eb.setIdAttribute("Id", true);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        List<Reference> refs = new ArrayList<Reference>();
        refs.add(sf.newReference("#e1", dm, null, null, "ref-e1"));
        refs.add(sf.newReference("#e2", dm, null, null, "ref-e2"));
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), refs);
        XMLSignature sig = sf.newXMLSignature(si, null);
        sig.sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        // digest values computed and stored by sign()
        Reference s0 = (Reference) sig.getSignedInfo().getReferences().get(0);
        Reference s1 = (Reference) sig.getSignedInfo().getReferences().get(1);
        check(s0.getDigestValue().length == 32, "ref-e1 digest 32 bytes");
        check(s1.getDigestValue().length == 32, "ref-e2 digest 32 bytes");
        check(!Arrays.equals(s0.getDigestValue(), s1.getDigestValue()), "different content -> different digests");
        // both validate
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "two-reference signature validates");
        Reference v0 = (Reference) v.getSignedInfo().getReferences().get(0);
        Reference v1 = (Reference) v.getSignedInfo().getReferences().get(1);
        check(v0.validate(vc), "ref-e1 valid");
        check(v1.validate(vc), "ref-e2 valid");
        check(Arrays.equals(v0.getDigestValue(), v0.getCalculatedDigestValue()), "stored == calculated (e1)");
        // tamper only e1 -> only its reference fails; e2 unaffected
        ea.setTextContent("HACKED");
        DOMValidateContext vc2 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v2 = sf.unmarshalXMLSignature(vc2);
        Reference w0 = (Reference) v2.getSignedInfo().getReferences().get(0);
        Reference w1 = (Reference) v2.getSignedInfo().getReferences().get(1);
        check(!w0.validate(vc2), "tampered e1 -> its reference invalid");
        check(w1.validate(vc2), "untampered e2 reference still valid");
        check(!v2.validate(vc2), "overall signature invalid");
        check(!Arrays.equals(w0.getDigestValue(), w0.getCalculatedDigestValue()), "stored != calculated after tamper");
    }

    /** cacheReference property exposes dereferenced data and digest input stream. */
    static void testCachedReferenceData() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r><c Id=\"cc\">TEXT</c></r>");
        Element c = (Element) doc.getElementsByTagName("c").item(0); c.setIdAttribute("Id", true);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null),
            Collections.singletonList(sf.newReference("#cc", dm)));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        vc.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "cached-reference validation passes");
        Reference r = (Reference) v.getSignedInfo().getReferences().get(0);
        Data dd = r.getDereferencedData();
        check(dd != null, "dereferenced data cached");
        InputStream dis = r.getDigestInputStream();
        check(dis != null, "digest input stream available");
        // dereferenced data of an id reference is a node-set
        check(dd instanceof NodeSetData, "id reference dereferences to NodeSetData");
        java.util.Iterator<?> it = ((NodeSetData) dd).iterator();
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        check(n >= 1, "node-set iterator yields nodes: " + n);
        // digest input stream carries the (canonicalized) octets of the referenced element
        byte[] octets = read(dis);
        String text = new String(octets, "UTF-8");
        check(text.contains("TEXT"), "digest octets contain referenced content");
    }

    static byte[] read(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        int c;
        while ((c = in.read()) >= 0) b.write(c);
        return b.toByteArray();
    }

    /** BASE64 transform decodes content before digesting. */
    static void testBase64TransformReference() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r><b64 Id=\"bb\">SGVsbG8gQmFzZTY0IQ==</b64></r>");
        Element b64 = (Element) doc.getElementsByTagName("b64").item(0); b64.setIdAttribute("Id", true);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("#bb", dm,
            Collections.singletonList(sf.newTransform(Transform.BASE64, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        vc.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "base64-transformed reference validates");
        Reference r = (Reference) v.getSignedInfo().getReferences().get(0);
        byte[] decoded = read(r.getDigestInputStream());
        check(new String(decoded, "UTF-8").equals("Hello Base64!"), "base64 transform decoded octets: '" + new String(decoded, "UTF-8") + "'");
        // tamper the base64 payload
        b64.setTextContent("WEVSTEw=");
        DOMValidateContext vc2 = new DOMValidateContext(KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(!sf.unmarshalXMLSignature(vc2).validate(vc2), "base64 payload tamper detected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetterMatrix();
        testDigestComputationAndGranularity();
        testCachedReferenceData();
        testBase64TransformReference();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
