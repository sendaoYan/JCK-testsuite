/*
 * @test
 * @summary javax.xml.crypto.dsig.Transform conformance.
 * @run main TransformTest
 */

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dom.DOMStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Collections;

public class TransformTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.Transform");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false, am = false;
        for (String i : ifs) { if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true; if (i.equals("javax.xml.crypto.AlgorithmMethod")) am = true; }
        check(xmls && am, "implements XMLStructure and AlgorithmMethod");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getParameterSpec"), "has getParameterSpec");
        check(names.contains("transform"), "has transform");
    }

    static void testConstantsAndInstantiation() throws Exception {
        check(Transform.BASE64.equals("http://www.w3.org/2000/09/xmldsig#base64"), "BASE64 constant");
        check(Transform.ENVELOPED.equals("http://www.w3.org/2000/09/xmldsig#enveloped-signature"), "ENVELOPED constant");
        check(Transform.XPATH.equals("http://www.w3.org/TR/1999/REC-xpath-19991116"), "XPATH constant");
        check(Transform.XPATH2.equals("http://www.w3.org/2002/06/xmldsig-filter2"), "XPATH2 constant");
        check(Transform.XSLT.equals("http://www.w3.org/TR/1999/REC-xslt-19991116"), "XSLT constant");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        // parameterless transforms: BASE64, ENVELOPED; spec must be null
        for (String uri : new String[] {Transform.BASE64, Transform.ENVELOPED}) {
            Transform t = sf.newTransform(uri, (TransformParameterSpec) null);
            check(t.getAlgorithm().equals(uri), "getAlgorithm round-trip " + uri);
            check(t.getParameterSpec() == null, "null spec for " + uri);
        }
        // EXC_C14N transform via newTransform with ExcC14NParameterSpec
        Transform exc = sf.newTransform(CanonicalizationMethod.EXCLUSIVE,
            new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec(Collections.singletonList("px")));
        check(exc.getAlgorithm().equals(CanonicalizationMethod.EXCLUSIVE), "EXC transform algorithm");
        check(exc.getParameterSpec() instanceof javax.xml.crypto.dsig.spec.ExcC14NParameterSpec, "EXC transform spec type");
        // XPATH: XPathFilterParameterSpec round-trip
        XPathFilterParameterSpec xp = new XPathFilterParameterSpec("ancestor-or-self::n");
        Transform tx = sf.newTransform(Transform.XPATH, xp);
        check(((XPathFilterParameterSpec) tx.getParameterSpec()).getXPath().equals("ancestor-or-self::n"), "XPath spec round-trip");
        // XPATH2: XPathFilter2ParameterSpec round-trip
        XPathFilter2ParameterSpec x2 = new XPathFilter2ParameterSpec(Collections.singletonList(
            new XPathType("here", XPathType.Filter.INTERSECT)));
        Transform tx2 = sf.newTransform(Transform.XPATH2, x2);
        check(((XPathFilter2ParameterSpec) tx2.getParameterSpec()).getXPathList().size() == 1, "XPath2 spec round-trip");
        // XSLT: stylesheet XMLStructure round-trip
        Document d = newDoc("<r/>");
        org.w3c.dom.Element ss = d.createElementNS("http://www.w3.org/1999/XSL/Transform", "stylesheet");
        XSLTTransformParameterSpec xs = new XSLTTransformParameterSpec(new DOMStructure(ss));
        Transform ts = sf.newTransform(Transform.XSLT, xs);
        check(ts.getParameterSpec() instanceof XSLTTransformParameterSpec, "XSLT spec type");
        check(((DOMStructure) ((XSLTTransformParameterSpec) ts.getParameterSpec()).getStylesheet()).getNode() == ss,
            "XSLT stylesheet identity round-trip");
        // negatives
        try { sf.newTransform(Transform.XPATH, (TransformParameterSpec) null); check(false, "XPATH requires spec"); }
        catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage().contains("params are required"), "XPATH params required message");
        }
        try { sf.newTransform("http://example.com/tf", (TransformParameterSpec) null); check(false, "unregistered transform"); }
        catch (java.security.NoSuchAlgorithmException e) {
            check(e.getMessage().contains("http://example.com/tf"), "unregistered transform message contains URI");
        }
        // wrong-typed spec: ENVELOPED accepts no params
        try { sf.newTransform(Transform.ENVELOPED, xp); check(false, "ENVELOPED rejects params"); }
        catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage() != null, "ENVELOPED rejects params with message: " + e.getMessage());
        }
    }

    /** ENVELOPED transform semantics: signature removal keeps whole-document reference valid. */
    static void testEnvelopedSemantics() throws Exception {
        KeyPair kp = rsaKP();
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r>body</r>");
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = sf.newReference("", dm,
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        javax.xml.crypto.dsig.dom.DOMValidateContext vc =
            new javax.xml.crypto.dsig.dom.DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "enveloped-transform whole-doc reference validates");
        // without the enveloped transform the same document cannot validate (signature affects the digest)
        Document doc2 = newDoc("<r>body</r>");
        Reference ref2 = sf.newReference("", dm);
        SignedInfo si2 = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref2));
        sf.newXMLSignature(si2, null).sign(new DOMSignContext(kp.getPrivate(), doc2.getDocumentElement()));
        Element sigEl2 = (Element) doc2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        javax.xml.crypto.dsig.dom.DOMValidateContext vc2 =
            new javax.xml.crypto.dsig.dom.DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl2);
        check(!sf.unmarshalXMLSignature(vc2).validate(vc2), "whole-doc reference without enveloped transform cannot validate");
    }

    /**
     * transform(Data, ctx) contract: a factory-fresh Transform is not yet bound to a DOM node
     * and rejects transformation; a Transform unmarshalled from a signature executes BASE64 decoding.
     */
    static void testDirectTransformApplication() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Transform b64 = sf.newTransform(Transform.BASE64, (TransformParameterSpec) null);
        byte[] raw = "QXJjaGl2ZQ==".getBytes("UTF-8"); // "Archive"
        javax.xml.crypto.dsig.dom.DOMValidateContext ctx =
            new javax.xml.crypto.dsig.dom.DOMValidateContext(
                javax.xml.crypto.KeySelector.singletonKeySelector(rsaKP().getPublic()), newDoc("<r/>").getDocumentElement());
        // negative: not marshalled yet
        try {
            b64.transform(new OctetStreamData(new ByteArrayInputStream(raw)), ctx);
            check(false, "factory-fresh transform must reject transform()");
        } catch (javax.xml.crypto.dsig.TransformException e) {
            check(e.getMessage() != null, "unmarshalled-transform rejection message: " + e.getMessage());
        }
        // positive: BASE64 transform inside a signed document, then executed directly
        KeyPair kp = rsaKP();
        Document doc = newDoc("<r><b64 Id=\"bb\">QXJjaGl2ZQ==</b64></r>");
        Element b64El = (Element) doc.getElementsByTagName("b64").item(0);
        b64El.setIdAttribute("Id", true);
        javax.xml.crypto.dsig.DigestMethod dm = sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null);
        javax.xml.crypto.dsig.Reference ref = sf.newReference("#bb", dm,
            java.util.Collections.singletonList(sf.newTransform(Transform.BASE64, (TransformParameterSpec) null)), null, null);
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), java.util.Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        javax.xml.crypto.dsig.dom.DOMValidateContext vc =
            new javax.xml.crypto.dsig.dom.DOMValidateContext(javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        javax.xml.crypto.dsig.XMLSignature v = sf.unmarshalXMLSignature(vc);
        check(v.validate(vc), "base64-signed document validates");
        Transform vtb = (Transform) ((javax.xml.crypto.dsig.Reference) v.getSignedInfo().getReferences().get(0)).getTransforms().get(0);
        check(vtb.getAlgorithm().equals(Transform.BASE64), "unmarshalled transform algorithm");
        Data out = vtb.transform(new OctetStreamData(new ByteArrayInputStream(raw)), vc);
        check(out instanceof OctetStreamData, "marshalled base64 transform yields OctetStreamData");
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        int c;
        java.io.InputStream in = ((OctetStreamData) out).getOctetStream();
        while ((c = in.read()) >= 0) buf.write(c);
        check(new String(buf.toByteArray(), "UTF-8").equals("Archive"), "marshalled base64 transform decodes payload");
        // streaming overload writes to the provided OutputStream
        java.io.ByteArrayOutputStream sink = new java.io.ByteArrayOutputStream();
        vtb.transform(new OctetStreamData(new ByteArrayInputStream(raw)), vc, sink);
        check(new String(sink.toByteArray(), "UTF-8").equals("Archive"), "streaming overload decodes payload");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstantsAndInstantiation();
        testEnvelopedSemantics();
        testDirectTransformApplication();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
