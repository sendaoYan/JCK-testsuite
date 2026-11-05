/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathFilterParameterSpec conformance.
 * @run main XPathFilterParameterSpecTest
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
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class XPathFilterParameterSpecTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XPathFilterParameterSpec");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean tp = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.dsig.spec.TransformParameterSpec")) tp = true;
        check(tp, "implements TransformParameterSpec");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getXPath"), "has getXPath");
        check(names.contains("getNamespaceMap"), "has getNamespaceMap");
    }

    static void testSemantics() {
        XPathFilterParameterSpec s1 = new XPathFilterParameterSpec("ancestor-or-self::n");
        check(s1.getXPath().equals("ancestor-or-self::n"), "xpath round-trip");
        check(s1.getNamespaceMap().isEmpty(), "default namespace map empty");
        Map<String, String> ns = new HashMap<String, String>();
        ns.put("p", "urn:p");
        XPathFilterParameterSpec s2 = new XPathFilterParameterSpec("p:e", ns);
        check(s2.getXPath().equals("p:e"), "xpath with ns round-trip");
        check(s2.getNamespaceMap().size() == 1 && "urn:p".equals(s2.getNamespaceMap().get("p")), "namespace map round-trip");
        // explicit empty namespace map -> empty (immutable) map
        XPathFilterParameterSpec s3 = new XPathFilterParameterSpec("e", new HashMap<String, String>());
        check(s3.getNamespaceMap() != null && s3.getNamespaceMap().isEmpty(), "empty map stays empty");
        // null namespace map rejected
        try { new XPathFilterParameterSpec("e", (java.util.Map<String, String>) null); check(false, "null map"); }
        catch (NullPointerException e) { check(true, "null map NPE"); }
        // source map copied
        ns.put("q", "urn:q");
        check(s2.getNamespaceMap().size() == 1, "source map copied in");
        // returned map immutable
        try { s2.getNamespaceMap().put("x", "urn:x"); check(false, "namespace map immutable"); }
        catch (UnsupportedOperationException e) { check(true, "namespace map immutable"); }
        // null xpath rejected
        try { new XPathFilterParameterSpec(null); check(false, "null xpath"); }
        catch (NullPointerException e) { check(true, "null xpath NPE"); }
        // null key/value in map rejected
        try {
            Map<String, String> bad = new HashMap<String, String>();
            bad.put(null, "urn:v");
            new XPathFilterParameterSpec("e", bad);
            check(false, "null ns prefix");
        } catch (RuntimeException e) { check(true, "null ns prefix rejected with " + e.getClass().getSimpleName()); }
    }

    /** XPath transform inside a real signature: spec round-trips, digest covers the selected node set. */
    static void testSignedXPathRoundTrip() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r><t Id=\"xp\">S</t></r>");
        Element t = (Element) doc.getElementsByTagName("t").item(0); t.setIdAttribute("Id", true);
        Map<String, String> ns = Collections.singletonMap("x", "urn:x");
        XPathFilterParameterSpec spec = new XPathFilterParameterSpec("ancestor-or-self::t", ns);
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        javax.xml.crypto.dsig.Transform tr = sf.newTransform(javax.xml.crypto.dsig.Transform.XPATH, spec);
        check(((XPathFilterParameterSpec) tr.getParameterSpec()).getXPath().equals("ancestor-or-self::t"), "transform spec xpath round-trip");
        check(((XPathFilterParameterSpec) tr.getParameterSpec()).getNamespaceMap().get("x").equals("urn:x"), "transform spec ns round-trip");
        Reference ref = sf.newReference("#xp", dm, Collections.singletonList(tr), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "xpath-filtered reference validates");
        Transform vt = (Transform) ((Reference) v.getSignedInfo().getReferences().get(0)).getTransforms().get(0);
        check(vt.getAlgorithm().equals(javax.xml.crypto.dsig.Transform.XPATH), "unmarshalled transform is XPATH");
        check(((XPathFilterParameterSpec) vt.getParameterSpec()).getXPath().equals("ancestor-or-self::t"),
            "unmarshalled xpath expression round-trip");
        check(((XPathFilterParameterSpec) vt.getParameterSpec()).getNamespaceMap().get("x").equals("urn:x"),
            "unmarshalled namespace map round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSemantics();
        testSignedXPathRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
