/*
 * @test
 * @summary javax.xml.crypto.URIDereferencer conformance.
 * @run main URIDereferencerTest
 */

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
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

public class URIDereferencerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    /** Records and delivers a fixed payload. */
    static class RecordingDereferencer implements URIDereferencer {
        final List<String> uris = new ArrayList<String>();
        final List<XMLCryptoContext> contexts = new ArrayList<XMLCryptoContext>();
        final byte[] payload;
        RecordingDereferencer(String payload) { this.payload = payload.getBytes(); }
        public Data dereference(URIReference ref, XMLCryptoContext ctx) {
            uris.add(ref.getURI());
            contexts.add(ctx);
            return new OctetStreamData(new ByteArrayInputStream(payload), ref.getURI(), "text/plain");
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.URIDereferencer");
        check(c.isInterface(), "is interface");
        check(c.getInterfaces().length == 0, "extends nothing");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("dereference"), "has dereference");

        // both factories expose a default dereferencer
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        check(sf.getURIDereferencer() != null, "signature factory default dereferencer non-null");
        check(sf.getKeyInfoFactory().getURIDereferencer() != null, "keyinfo factory default dereferencer non-null");

        // custom dereferencer participates in digest computation at sign and validate
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RecordingDereferencer ud = new RecordingDereferencer("custom-delivered-payload");
        Reference ref = sf.newReference("#delivered", sf.newDigestMethod(DigestMethod.SHA256, null));
        SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(ref));
        Document doc = newDoc("<r/>");
        DOMSignContext sc = new DOMSignContext(kp.getPrivate(), doc.getDocumentElement());
        sc.setURIDereferencer(ud);
        sf.newXMLSignature(si, null).sign(sc);
        check(ud.uris.size() == 1 && "#delivered".equals(ud.uris.get(0)), "dereferencer called at sign with the reference URI");
        check(ud.contexts.get(0) == sc, "dereferencer receives the sign context by identity");
        Element sigEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc = new DOMValidateContext(
            javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        vc.setURIDereferencer(ud);
        check(sf.unmarshalXMLSignature(vc).validate(vc), "same payload at validate keeps signature valid");
        check(ud.uris.size() == 2, "dereferencer called once more at validate");

        // changing the delivered payload invalidates the digest
        RecordingDereferencer tampered = new RecordingDereferencer("TAMPERED-payload");
        DOMValidateContext vc2 = new DOMValidateContext(
            javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        vc2.setURIDereferencer(tampered);
        check(!sf.unmarshalXMLSignature(vc2).validate(vc2), "different payload invalidates signature");

        // default dereferencer resolves same-document references without custom registration
        Document doc2 = newDoc("<r><t Id=\"t9\">LOCAL</t></r>");
        Element t9 = (Element) doc2.getElementsByTagName("t").item(0);
        t9.setIdAttribute("Id", true);
        Reference localRef = sf.newReference("#t9", sf.newDigestMethod(DigestMethod.SHA256, null));
        SignedInfo si2 = sf.newSignedInfo(
            sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
            sf.newSignatureMethod(RSA_SHA256, null), Collections.singletonList(localRef));
        sf.newXMLSignature(si2, null).sign(new DOMSignContext(kp.getPrivate(), doc2.getDocumentElement()));
        Element sigEl2 = (Element) doc2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
        DOMValidateContext vc3 = new DOMValidateContext(
            javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl2);
        check(sf.unmarshalXMLSignature(vc3).validate(vc3), "default dereferencer resolves same-document URI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
