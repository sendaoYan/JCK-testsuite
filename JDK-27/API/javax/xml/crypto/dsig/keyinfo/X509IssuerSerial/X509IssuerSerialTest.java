/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.X509IssuerSerial conformance.
 * @run main X509IssuerSerialTest
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
import java.util.Collections;

public class X509IssuerSerialTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.X509IssuerSerial");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true;
        check(xmls, "implements XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getIssuerName"), "has getIssuerName");
        check(names.contains("getSerialNumber"), "has getSerialNumber");
    }

    static void testSemantics() throws Exception {
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        BigInteger serial = new BigInteger("16909060"); // 0x01020304
        X509IssuerSerial is = kif.newX509IssuerSerial("CN=Demo Issuer, O=Org", serial);
        check(is.getIssuerName().equals("CN=Demo Issuer, O=Org"), "issuer round-trip");
        check(is.getSerialNumber().equals(serial), "serial round-trip");
        // serial edge values
        check(kif.newX509IssuerSerial("CN=x", BigInteger.ZERO).getSerialNumber()
            .equals(BigInteger.ZERO), "zero serial");
        check(kif.newX509IssuerSerial("CN=x", BigInteger.ONE.shiftLeft(128)).getSerialNumber()
            .bitLength() == 129, "large serial preserved");
        // empty issuer name accepted by factory (validated only as X500 name where applicable)
        X509IssuerSerial e = kif.newX509IssuerSerial("", BigInteger.ONE);
        check(e.getIssuerName().equals(""), "empty issuer round-trip");
        // negatives with precise messages
        try { kif.newX509IssuerSerial(null, BigInteger.ONE); check(false, "null issuer"); }
        catch (NullPointerException npe) { check(npe.getMessage().contains("issuerName"), "null issuer message"); }
        try { kif.newX509IssuerSerial("CN=x", null); check(false, "null serial"); }
        catch (NullPointerException npe) { check(npe.getMessage().contains("serialNumber"), "null serial message"); }
        // feature contract
        try { is.isFeatureSupported(null); check(false, "null feature"); }
        catch (NullPointerException npe) { check(true, "isFeatureSupported(null) NPE"); }
        check(!is.isFeatureSupported("f"), "unknown feature false");
    }

    static void testMarshalRoundTrip() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r/>");
        X509IssuerSerial is = kif.newX509IssuerSerial("CN=Demo Issuer", new BigInteger("42"));
        X509Data x5 = kif.newX509Data(Collections.singletonList((Object) is));
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
        Element isEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "X509IssuerSerial").item(0);
        check(isEl != null, "X509IssuerSerial marshalled");
        check(isEl.getElementsByTagNameNS(XMLSignature.XMLNS, "X509IssuerName").getLength() == 1, "X509IssuerName child");
        Element snEl = (Element) isEl.getElementsByTagNameNS(XMLSignature.XMLNS, "X509SerialNumber").item(0);
        check(snEl != null && snEl.getTextContent().equals("42"), "X509SerialNumber text round-trip");
        KeyInfo back = kif.unmarshalKeyInfo(new DOMStructure(
            (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo").item(0)));
        X509Data vx = (X509Data) back.getContent().get(0);
        X509IssuerSerial vis = (X509IssuerSerial) vx.getContent().get(0);
        check(vis.getIssuerName().equals("CN=Demo Issuer"), "unmarshalled issuer name");
        check(vis.getSerialNumber().equals(new BigInteger("42")), "unmarshalled serial number");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSemantics();
        testMarshalRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
