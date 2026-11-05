/*
 * @test
 * @summary javax.xml.crypto.dsig.dom.DOMSignContext conformance: DOM sign context.
 * @run main DOMSignContextTest
 */

import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collections;

public class DOMSignContextTest {
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
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            KP = g.generateKeyPair();
        }
        return KP;
    }

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    /** Signs an enveloped RSA-SHA256 signature into doc using the given context. */
    static void sign(Document d, KeyPair kp, DOMSignContext ctx) throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Reference ref = sf.newReference("", sf.newDigestMethod(DigestMethod.SHA256, null),
            Collections.singletonList(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        sf.newXMLSignature(si, null).sign(ctx);
    }

    static String childNames(Element e) {
        StringBuilder sb = new StringBuilder();
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) sb.append(n.getNodeName()).append(" ");
        return sb.toString().trim();
    }

    static String serialize(Document d) throws Exception {
        StringWriter sw = new StringWriter();
        javax.xml.transform.TransformerFactory.newInstance().newTransformer()
            .transform(new javax.xml.transform.dom.DOMSource(d), new javax.xml.transform.stream.StreamResult(sw));
        return sw.toString();
    }

    public static void main(String[] args) throws Exception {
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        org.w3c.dom.Node node = doc.createElement("root");
        java.security.Key key = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        javax.xml.crypto.dsig.dom.DOMSignContext c = new javax.xml.crypto.dsig.dom.DOMSignContext(key, node);
        check(c.getParent() == node, "parent");
        check(c.getNextSibling() == null, "next sibling null");
        check(c instanceof javax.xml.crypto.dsig.XMLSignContext, "is XMLSignContext");
        check(c instanceof javax.xml.crypto.dom.DOMCryptoContext, "extends DOMCryptoContext");
        check(c instanceof javax.xml.crypto.XMLCryptoContext, "is XMLCryptoContext");

        // --- null matrix with precise messages ---
        KeyPair kp = rsaKP();
        try {
            DOMSignContext t = new DOMSignContext((Key) null, node);
            check(false, "null key should throw: " + t);
        } catch (NullPointerException e) { check("signingKey cannot be null".equals(e.getMessage()), "null key message"); }
        try {
            DOMSignContext t = new DOMSignContext((Key) new SecretKeySpec(new byte[1], "X"), (Node) null);
            check(false, "null parent should throw: " + t);
        } catch (NullPointerException e) { check("parent cannot be null".equals(e.getMessage()), "null parent message"); }
        try {
            DOMSignContext t = new DOMSignContext((Key) new SecretKeySpec(new byte[1], "X"), node, (Node) null);
            check(false, "null nextSibling should throw: " + t);
        } catch (NullPointerException e) { check("nextSibling cannot be null".equals(e.getMessage()), "null nextSibling message"); }
        try {
            DOMSignContext t = new DOMSignContext((javax.xml.crypto.KeySelector) null, node);
            check(false, "null selector should throw: " + t);
        } catch (NullPointerException e) { check("key selector cannot be null".equals(e.getMessage()), "null selector message"); }
        try { c.setParent(null); check(false, "setParent(null) should throw"); }
        catch (NullPointerException e) { check("parent is null".equals(e.getMessage()), "setParent(null) message"); }

        // --- insertion position control: constructor with nextSibling inserts before that node ---
        Document d1 = newDoc("<r><a>1</a><b>2</b></r>");
        Element b1 = (Element) d1.getElementsByTagName("b").item(0);
        sign(d1, kp, new DOMSignContext(kp.getPrivate(), d1.getDocumentElement(), b1));
        check(childNames(d1.getDocumentElement()).equals("a Signature b"), "ctor nextSibling insertion: a Signature b");

        // --- setNextSibling achieves the same control after construction ---
        Document d2 = newDoc("<r><a>1</a><b>2</b></r>");
        DOMSignContext c2 = new DOMSignContext(kp.getPrivate(), d2.getDocumentElement());
        c2.setNextSibling(d2.getElementsByTagName("b").item(0));
        check(c2.getNextSibling() == d2.getElementsByTagName("b").item(0), "setNextSibling readback identity");
        sign(d2, kp, c2);
        check(childNames(d2.getDocumentElement()).equals("a Signature b"), "setNextSibling insertion: a Signature b");

        // --- default position appends last ---
        Document d3 = newDoc("<r><a>1</a><b>2</b></r>");
        sign(d3, kp, new DOMSignContext(kp.getPrivate(), d3.getDocumentElement()));
        check(childNames(d3.getDocumentElement()).equals("a b Signature"), "default append position: a b Signature");

        // --- setParent redirects the insertion point ---
        Document d4 = newDoc("<r><a>1</a><b>2</b></r>");
        DOMSignContext c4 = new DOMSignContext(kp.getPrivate(), d4.getDocumentElement());
        c4.setParent(d4.getElementsByTagName("a").item(0));
        check(c4.getParent() == d4.getElementsByTagName("a").item(0), "setParent readback identity");
        sign(d4, kp, c4);
        check("1 Signature".equals(childNames((Element) d4.getElementsByTagName("a").item(0)).replace("#text", "1"))
            || d4.getElementsByTagName("a").item(0).getLastChild().getLocalName().equals("Signature"),
            "setParent inserts into redirected parent");

        // --- prefix control: ds prefix appears in serialized output ---
        Document d5 = newDoc("<r>X</r>");
        DOMSignContext c5 = new DOMSignContext(kp.getPrivate(), d5.getDocumentElement());
        check(c5.getDefaultNamespacePrefix() == null, "default namespace prefix null");
        c5.setDefaultNamespacePrefix("ds");
        check("ds".equals(c5.getDefaultNamespacePrefix()), "setDefaultNamespacePrefix readback");
        sign(d5, kp, c5);
        check(serialize(d5).contains("ds:Signature"), "serialized output uses ds:Signature");
        check(d5.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").getLength() == 1, "dsig namespace preserved under prefix");

        // --- putNamespacePrefix registration ---
        c5.putNamespacePrefix(XMLSignature.XMLNS, "zz");
        check("zz".equals(c5.getNamespacePrefix(XMLSignature.XMLNS, "dflt")), "putNamespacePrefix overrides");
        check("dflt".equals(c5.getNamespacePrefix("urn:other", "dflt")), "unregistered prefix returns default");

        // --- baseURI / properties / dereferencer ---
        Document d6 = newDoc("<r/>");
        DOMSignContext c6 = new DOMSignContext(kp.getPrivate(), d6.getDocumentElement());
        check(c6.getBaseURI() == null, "baseURI default null");
        c6.setBaseURI("http://base/");
        check("http://base/".equals(c6.getBaseURI()), "baseURI round-trip");
        check(c6.setProperty("k", "v1") == null, "setProperty returns previous (null)");
        check("v1".equals(c6.setProperty("k", "v2")), "setProperty returns previous value");
        check("v2".equals(c6.getProperty("k")), "getProperty round-trip");
        check(c6.getProperty("nope") == null, "missing property null");
        URIDereferencer ud = new URIDereferencer() {
            public javax.xml.crypto.Data dereference(javax.xml.crypto.URIReference r, javax.xml.crypto.XMLCryptoContext x) { return null; }
        };
        check(c6.getURIDereferencer() == null, "no explicit dereferencer before set");
        c6.setURIDereferencer(ud);
        check(c6.getURIDereferencer() == ud, "URIDereferencer identity round-trip");
        // Key-based constructor supplies a singleton key selector for the sign operation
        check(new DOMSignContext(kp.getPrivate(), d6.getDocumentElement()).getKeySelector() != null,
            "key-based ctor wraps key in a key selector");

        // --- put/get semantics ---
        c6.put("a", "b");
        c6.put("a", "c");
        check("c".equals(c6.get("a")), "put overwrites previous mapping");
        check(c6.get("missing") == null, "missing mapping null");
        // id-mapping iterator is empty until ids are registered
        check(!c6.iterator().hasNext(), "id-mapping iterator empty initially");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
