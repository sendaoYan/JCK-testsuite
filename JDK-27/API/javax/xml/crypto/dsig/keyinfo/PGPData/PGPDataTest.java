/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.PGPData conformance.
 * @run main PGPDataTest
 */

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.PGPData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Collections;

public class PGPDataTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.PGPData");
        check(c.isInterface(), "is interface");
        check(PGPData.TYPE.equals("http://www.w3.org/2000/09/xmldsig#PGPData"), "TYPE constant");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true;
        check(xmls, "implements XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getKeyId"), "has getKeyId");
        check(names.contains("getKeyPacket"), "has getKeyPacket");
        check(names.contains("getExternalElements"), "has getExternalElements");
    }

    static void testConstruction() throws Exception {
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        byte[] keyId = {1, 2, 3, 4, 5, 6, 7, 8};
        // minimal form
        PGPData p1 = kif.newPGPData(keyId);
        check(p1.getKeyId() != null, "keyId present");
        check(p1.getKeyId().length == 8, "keyId 8 bytes");
        check(p1.getKeyPacket() == null, "keyPacket null minimal form");
        check(p1.getExternalElements().isEmpty(), "external elements empty minimal form");
        // defensive copy in: caller mutation invisible
        keyId[0] = 99;
        check(p1.getKeyId()[0] == 1, "input array copied in");
        // defensive copy out: returned array mutation invisible
        byte[] out = p1.getKeyId();
        out[1] = 77;
        check(p1.getKeyId()[1] == 2, "returned array copied out");
        // keyId + keyPacket form; packet must be >= 3 bytes
        PGPData p2 = kif.newPGPData(new byte[8], new byte[] {(byte) 0xC5, 11, 12}, null);
        check(p2.getKeyId().length == 8, "p2 keyId");
        check(p2.getKeyPacket().length == 3 && p2.getKeyPacket()[0] == (byte) 0xC5, "keyPacket round-trip");
        check(p2.getExternalElements().isEmpty(), "p2 no external elements");
        try { kif.newPGPData(new byte[8], new byte[] {(byte) 0xC5, 0x01}, null); check(false, "short packet"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("3 bytes"), "short keyPacket message"); }
        // keyPacket-only form
        PGPData p3 = kif.newPGPData(new byte[] {(byte) 0xC5, 22, 23}, Collections.<XMLStructure>emptyList());
        check(p3.getKeyId() == null, "packet-only form has no keyId");
        check(p3.getKeyPacket().length == 3, "packet-only form packet");
        // external elements preserved
        Document d = newDoc("<r/>");
        DOMStructure ext = new DOMStructure(d.createElementNS("urn:pgp", "ext"));
        PGPData p4 = kif.newPGPData(new byte[8], (byte[]) null, Collections.singletonList(ext));
        check(p4.getExternalElements().size() == 1, "external element preserved");
        check(p4.getExternalElements().get(0) == ext, "external element identity");
        try { p4.getExternalElements().clear(); check(false, "external elements immutable"); }
        catch (UnsupportedOperationException e) { check(true, "external elements immutable UOE"); }
        // negatives
        try { kif.newPGPData(null); check(false, "null keyId"); }
        catch (NullPointerException e) { check(e.getMessage().contains("keyId"), "null keyId message"); }
        try { kif.newPGPData(new byte[] {1, 2, 3}); check(false, "short keyId"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("8 bytes"), "short keyId message"); }
        try { kif.newPGPData(null, new byte[] {(byte) 0xC5, 0x01, 0x02}, null); check(false, "null keyId with packet"); }
        catch (NullPointerException e) { check(true, "null keyId with packet NPE"); }
        // feature contract
        try { p1.isFeatureSupported(null); check(false, "null feature"); }
        catch (NullPointerException e) { check(true, "isFeatureSupported(null) NPE"); }
        check(!p1.isFeatureSupported("x"), "unknown feature false");
    }

    static void testMarshalRoundTrip() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r/>");
        byte[] keyId = {9, 8, 7, 6, 5, 4, 3, 2};
        PGPData pgp = kif.newPGPData(keyId, new byte[] {(byte) 0xC5, 0x01, 0x02}, null);
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(pgp), "pgp-ki");
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
        Element pgpEl = (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "PGPData").item(0);
        check(pgpEl != null, "PGPData element marshalled");
        check(pgpEl.getElementsByTagNameNS(XMLSignature.XMLNS, "PGPKeyID").getLength() == 1, "PGPKeyID child");
        check(pgpEl.getElementsByTagNameNS(XMLSignature.XMLNS, "PGPKeyPacket").getLength() == 1, "PGPKeyPacket child");
        KeyInfo back = kif.unmarshalKeyInfo(new DOMStructure(
            (Element) sigEl.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo").item(0)));
        PGPData vp = (PGPData) back.getContent().get(0);
        check(Arrays.equals(vp.getKeyId(), keyId), "unmarshalled keyId round-trip");
        check(vp.getKeyPacket() != null && vp.getKeyPacket().length == 3, "unmarshalled keyPacket round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstruction();
        testMarshalRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
