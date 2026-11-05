/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.X509Data conformance.
 * @run main X509DataTest
 */

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import javax.xml.crypto.dom.DOMStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class X509DataTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.X509Data");
        check(c.isInterface(), "is interface");
        check(X509Data.TYPE.equals("http://www.w3.org/2000/09/xmldsig#X509Data"), "TYPE constant");
        check(X509Data.RAW_X509_CERTIFICATE_TYPE.equals("http://www.w3.org/2000/09/xmldsig#rawX509Certificate"), "RAW_X509_CERTIFICATE_TYPE constant");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getContent"), "has getContent");
    }

    static void testContentTypes() throws Exception {
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        X509IssuerSerial is = kif.newX509IssuerSerial("CN=Issuer", new BigInteger("777"));
        List<Object> content = new ArrayList<Object>();
        content.add("CN=SubjectName");                       // X509SubjectName (String)
        content.add(new byte[] {0x01, 0x02});                // X509SKI (byte[])
        content.add(is);                                     // X509IssuerSerial
        X509Data x = kif.newX509Data(content);
        check(x.getContent().size() == 3, "three content entries");
        check(x.getContent().get(0).equals("CN=SubjectName"), "subject name entry round-trip");
        check(((byte[]) x.getContent().get(1)).length == 2, "SKI entry round-trip");
        check(x.getContent().get(2) == is, "issuer-serial entry identity");
        // unknown type rejected
        try { kif.newX509Data(Collections.singletonList((Object) Long.valueOf(5))); check(false, "Long rejected"); }
        catch (ClassCastException e) { check(e.getMessage() != null, "invalid type CCE message"); }
        try { kif.newX509Data(Collections.singletonList((Object) "not a name")); check(false, "bad X500 name rejected"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null, "bad X500 name IAE"); }
        // null/empty
        try { kif.newX509Data(null); check(false, "null content"); }
        catch (NullPointerException e) { check(e.getMessage().contains("content"), "null content message"); }
        try { kif.newX509Data(new ArrayList<Object>()); check(false, "empty content"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("empty"), "empty content message"); }
        // content list immutable
        try { x.getContent().add("more"); check(false, "content immutable"); }
        catch (UnsupportedOperationException e) { check(true, "content immutable UOE"); }
    }

    static void testMarshalRoundTrip() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r/>");
        X509Data x5 = kif.newX509Data(Arrays.asList("CN=SubjectName",
            kif.newX509IssuerSerial("CN=Issuer", new BigInteger("777"))));
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x5));
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
        Element x5El = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "X509Data").item(0);
        check(x5El != null, "X509Data marshalled");
        check(x5El.getElementsByTagNameNS(XMLSignature.XMLNS, "X509SubjectName").getLength() == 1, "X509SubjectName child");
        check(x5El.getElementsByTagNameNS(XMLSignature.XMLNS, "X509IssuerSerial").getLength() == 1, "X509IssuerSerial child");
        KeyInfo back = kif.unmarshalKeyInfo(new DOMStructure(
            (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo").item(0)));
        X509Data vx = (X509Data) back.getContent().get(0);
        check(vx.getContent().size() == 2, "unmarshalled content size");
        check(vx.getContent().get(0).equals("CN=SubjectName"), "unmarshalled subject name");
        check(vx.getContent().get(1) instanceof X509IssuerSerial, "unmarshalled issuer-serial type");
        check(((X509IssuerSerial) vx.getContent().get(1)).getSerialNumber().equals(new BigInteger("777")), "unmarshalled serial");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testContentTypes();
        testMarshalRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
