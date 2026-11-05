/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec conformance.
 * @run main XPathFilter2ParameterSpecTest
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
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class XPathFilter2ParameterSpecTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean tp = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.dsig.spec.TransformParameterSpec")) tp = true;
        check(tp, "implements TransformParameterSpec");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getXPathList"), "has getXPathList");
    }

    static void testSemantics() {
        XPathType t1 = new XPathType("here()", XPathType.Filter.INTERSECT);
        XPathType t2 = new XPathType("there()", XPathType.Filter.UNION);
        XPathFilter2ParameterSpec s = new XPathFilter2ParameterSpec(Arrays.asList(t1, t2));
        check(s.getXPathList().size() == 2, "list size");
        check(s.getXPathList().get(0) == t1 && s.getXPathList().get(1) == t2, "identity and order");
        // source list copied
        List<XPathType> src = new ArrayList<XPathType>(Collections.singletonList(t1));
        XPathFilter2ParameterSpec s2 = new XPathFilter2ParameterSpec(src);
        src.add(t2);
        check(s2.getXPathList().size() == 1, "source list copied in");
        // returned list immutable
        try { s.getXPathList().clear(); check(false, "list immutable"); }
        catch (UnsupportedOperationException e) { check(true, "list immutable UOE"); }
        // single-element form
        XPathFilter2ParameterSpec s3 = new XPathFilter2ParameterSpec(Collections.singletonList(t1));
        check(s3.getXPathList().size() == 1, "single element");
        // negatives
        try { new XPathFilter2ParameterSpec(null); check(false, "null list"); }
        catch (NullPointerException e) { check(e.getMessage().contains("xPathList"), "null list message"); }
        try { new XPathFilter2ParameterSpec(new ArrayList<XPathType>()); check(false, "empty list"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("empty"), "empty list message"); }
    }

    /** XPATH2 transform inside a real signature with INTERSECT filter. */
    static void testSignedXPath2RoundTrip() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r><t Id=\"f2\">Z</t></r>");
        Element t = (Element) doc.getElementsByTagName("t").item(0); t.setIdAttribute("Id", true);
        XPathFilter2ParameterSpec spec = new XPathFilter2ParameterSpec(Collections.singletonList(
            new XPathType("ancestor-or-self::t", XPathType.Filter.INTERSECT)));
        Transform tr = sf.newTransform(Transform.XPATH2, spec);
        check(((XPathFilter2ParameterSpec) tr.getParameterSpec()).getXPathList().size() == 1, "factory transform spec round-trip");
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("#f2", dm, Collections.singletonList(tr), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "xpath2-filtered reference validates");
        Transform vt = (Transform) ((Reference) v.getSignedInfo().getReferences().get(0)).getTransforms().get(0);
        check(vt.getAlgorithm().equals(Transform.XPATH2), "unmarshalled XPATH2 algorithm");
        XPathType back = (XPathType) ((XPathFilter2ParameterSpec) vt.getParameterSpec()).getXPathList().get(0);
        check(back.getExpression().equals("ancestor-or-self::t"), "unmarshalled xpath2 expression");
        check(back.getFilter() == XPathType.Filter.INTERSECT, "unmarshalled xpath2 filter");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSemantics();
        testSignedXPath2RoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
