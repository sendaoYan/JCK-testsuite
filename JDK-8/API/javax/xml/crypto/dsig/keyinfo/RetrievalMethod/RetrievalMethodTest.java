/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.RetrievalMethod conformance.
 * @run main RetrievalMethodTest
 */

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dom.DOMStructure;
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

public class RetrievalMethodTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.RetrievalMethod");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false, ur = false;
        for (String i : ifs) { if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true; if (i.equals("javax.xml.crypto.URIReference")) ur = true; }
        check(xmls && ur, "implements XMLStructure and URIReference");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getURI"), "has getURI");
        check(names.contains("getType"), "has getType");
        check(names.contains("getTransforms"), "has getTransforms");
    }

    static void testGetterMatrix() throws Exception {
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        // minimal form
        RetrievalMethod r1 = kif.newRetrievalMethod("#key-loc");
        check(r1.getURI().equals("#key-loc"), "uri round-trip");
        check(r1.getType() == null, "type null default");
        check(r1.getTransforms() == null || r1.getTransforms().isEmpty(), "no transforms default (null or empty)");
        // with type and transforms
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        List<Transform> ts = new ArrayList<Transform>();
        ts.add(sf.newTransform(javax.xml.crypto.dsig.Transform.BASE64, (TransformParameterSpec) null));
        ts.add(sf.newTransform(javax.xml.crypto.dsig.Transform.ENVELOPED, (TransformParameterSpec) null));
        RetrievalMethod r2 = kif.newRetrievalMethod("http://example.com/keys/k1",
            "http://www.w3.org/2000/09/xmldsig#X509Data", ts);
        check(r2.getURI().equals("http://example.com/keys/k1"), "uri full form");
        check(r2.getType().equals("http://www.w3.org/2000/09/xmldsig#X509Data"), "type round-trip");
        check(r2.getTransforms().size() == 2, "two transforms");
        check(((Transform) r2.getTransforms().get(0)).getAlgorithm().equals(javax.xml.crypto.dsig.Transform.BASE64), "transform order");
        // transforms list is not a live view of the source
        ts.add(sf.newTransform(javax.xml.crypto.dsig.Transform.ENVELOPED, (TransformParameterSpec) null));
        check(r2.getTransforms().size() == 2, "source transform list mutation isolated");
        // URIReference contract
        javax.xml.crypto.URIReference asRef = r2;
        check(asRef.getURI().equals("http://example.com/keys/k1"), "URIReference getURI");
        // null URI rejected
        try { kif.newRetrievalMethod(null); check(false, "null uri"); }
        catch (NullPointerException e) { check(e.getMessage().contains("uri"), "null uri message"); }
    }

    static void testMarshalRoundTrip() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r/>");
        List<Transform> ts = Collections.singletonList(
            sf.newTransform(javax.xml.crypto.dsig.Transform.BASE64, (TransformParameterSpec) null));
        RetrievalMethod rm = kif.newRetrievalMethod("#kd", "http://www.w3.org/2000/09/xmldsig#KeyValue", ts);
        KeyInfo ki = kif.newKeyInfo(Arrays.asList(kif.newKeyName("n"), rm));
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
            Collections.singletonList(sf.newReference("#o",
                sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null))));
        javax.xml.crypto.dsig.XMLObject xo = sf.newXMLObject(null, "o", null, null);
        sf.newXMLSignature(si, ki, Collections.singletonList(xo), null, null)
            .sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        Element rmEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "RetrievalMethod").item(0);
        check(rmEl != null, "RetrievalMethod marshalled");
        check(rmEl.getAttribute("URI").equals("#kd"), "marshalled URI attribute");
        check(rmEl.getAttribute("Type").equals("http://www.w3.org/2000/09/xmldsig#KeyValue"), "marshalled Type attribute");
        check(rmEl.getElementsByTagNameNS(XMLSignature.XMLNS, "Transforms").getLength() == 1, "Transforms child marshalled");
        KeyInfo back = kif.unmarshalKeyInfo(new DOMStructure(
            (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo").item(0)));
        RetrievalMethod vrm = (RetrievalMethod) back.getContent().get(1);
        check(vrm.getURI().equals("#kd"), "unmarshalled URI round-trip");
        check(vrm.getType().equals("http://www.w3.org/2000/09/xmldsig#KeyValue"), "unmarshalled type round-trip");
        check(vrm.getTransforms() != null && vrm.getTransforms().size() == 1, "unmarshalled transforms round-trip");
        check(((Transform) vrm.getTransforms().get(0)).getAlgorithm().equals(javax.xml.crypto.dsig.Transform.BASE64),
            "unmarshalled transform algorithm");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetterMatrix();
        testMarshalRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
