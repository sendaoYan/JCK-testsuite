/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.KeyInfoFactory conformance.
 * @run main KeyInfoFactoryTest
 */

import javax.xml.crypto.NoSuchMechanismException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.PGPData;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KeyInfoFactoryTest {
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

    static void testFactoryMatrix() throws Exception {
        KeyInfoFactory kif = KeyInfoFactory.getInstance();
        check("DOM".equals(kif.getMechanismType()), "default mechanism DOM");
        check(kif.getProvider() != null, "provider non-null");
        KeyInfoFactory d1 = KeyInfoFactory.getInstance("DOM");
        check("DOM".equals(d1.getMechanismType()), "DOM mechanism");
        KeyInfoFactory d2 = KeyInfoFactory.getInstance("DOM", d1.getProvider());
        check(d2.getProvider() == d1.getProvider(), "provider overload identity");
        KeyInfoFactory d3 = KeyInfoFactory.getInstance("DOM", d1.getProvider().getName());
        check(d3.getProvider().getName().equals(d1.getProvider().getName()), "provider-name overload");
        // negatives
        try { KeyInfoFactory.getInstance("Bogus"); check(false, "bogus mechanism"); }
        catch (NoSuchMechanismException e) { check(e.getMessage().contains("Bogus"), "bogus mechanism message contains name"); }
        try { KeyInfoFactory.getInstance(null); check(false, "null mechanism"); }
        catch (NullPointerException e) { check(true, "null mechanism NPE"); }
        try { KeyInfoFactory.getInstance("DOM", (Provider) null); check(false, "null provider"); }
        catch (NullPointerException e) { check(true, "null provider NPE"); }
        try { KeyInfoFactory.getInstance("DOM", "NoSuchProviderQQ"); check(false, "unknown provider"); }
        catch (java.security.NoSuchProviderException e) { check(true, "unknown provider exception"); }
        // features + dereferencer
        try { kif.isFeatureSupported(null); check(false, "null feature"); }
        catch (NullPointerException e) { check(true, "null feature NPE"); }
        check(!kif.isFeatureSupported("javax.xml.crypto.dsig.cacheReference"), "cacheReference unsupported");
        check(!kif.isFeatureSupported("no.such.feature"), "unknown feature false");
        check(kif.getURIDereferencer() != null, "default URIDereferencer non-null");
    }

    static void testNewKeyInfoContent() throws Exception {
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        KeyPair kp = rsaKP();
        // KeyName
        KeyName kn = kif.newKeyName("my-key-name");
        check(kn.getName().equals("my-key-name"), "KeyName round-trip");
        try { kif.newKeyName(null); check(false, "null keyname"); }
        catch (NullPointerException e) { check(e.getMessage() != null, "newKeyName(null) NPE: " + e.getMessage()); }
        // KeyValue: RSA / DSA / EC round-trip equality
        check(kif.newKeyValue(kp.getPublic()).getPublicKey().equals(kp.getPublic()), "RSA KeyValue round-trip");
        KeyPairGenerator dpg = KeyPairGenerator.getInstance("DSA");
        dpg.initialize(1024);
        check(kif.newKeyValue(dpg.generateKeyPair().getPublic()).getPublicKey().getAlgorithm().equals("DSA"), "DSA KeyValue round-trip");
        KeyPairGenerator epg = KeyPairGenerator.getInstance("EC");
        epg.initialize(256);
        check(kif.newKeyValue(epg.generateKeyPair().getPublic()).getPublicKey().getAlgorithm().equals("EC"), "EC KeyValue round-trip");
        try { kif.newKeyValue(null); check(false, "null KeyValue"); }
        catch (NullPointerException e) { check(true, "newKeyValue(null) NPE"); }
        // unsupported key algorithm -> KeyException
        try {
            KeyPairGenerator dh = KeyPairGenerator.getInstance("DiffieHellman");
            kif.newKeyValue(dh.generateKeyPair().getPublic());
            check(false, "DH key should be rejected");
        } catch (java.security.KeyException e) {
            check(e.getMessage() != null && e.getMessage().contains("DH"), "KeyException message names algorithm");
        }
        // PGPData: exactly 8-byte key id
        PGPData pgp = kif.newPGPData(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});
        check(pgp.getKeyId().length == 8, "PGP keyId 8 bytes");
        check(pgp.getKeyPacket() == null, "PGP keyPacket null");
        check(pgp.getExternalElements().isEmpty(), "PGP no external elements");
        try { kif.newPGPData(new byte[] {1, 2, 3}); check(false, "short keyId"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("8 bytes"), "short keyId IAE message"); }
        try { kif.newPGPData(null); check(false, "null keyId"); }
        catch (NullPointerException e) { check(true, "null keyId NPE"); }
        PGPData pgp2 = kif.newPGPData(new byte[8], new byte[] {(byte) 0xC5, 0x01, 0x02}, null);
        check(pgp2.getKeyPacket().length == 3, "PGP keyPacket round-trip");
        // X509IssuerSerial
        X509IssuerSerial is = kif.newX509IssuerSerial("CN=Demo", new BigInteger("123456789"));
        check(is.getIssuerName().equals("CN=Demo"), "issuer name round-trip");
        check(is.getSerialNumber().equals(new BigInteger("123456789")), "serial round-trip");
        try { kif.newX509IssuerSerial(null, BigInteger.ONE); check(false, "null issuer"); }
        catch (NullPointerException e) { check(e.getMessage().contains("issuerName"), "null issuer message"); }
        try { kif.newX509IssuerSerial("CN=x", null); check(false, "null serial"); }
        catch (NullPointerException e) { check(e.getMessage().contains("serialNumber"), "null serial message"); }
        // X509Data valid content types + negatives
        X509Data x5 = kif.newX509Data(Arrays.asList("CN=Subject", new byte[] {9, 8}, is));
        check(x5.getContent().size() == 3, "X509Data mixed content size");
        check(x5.getContent().get(0).equals("CN=Subject"), "subject string round-trip");
        try { kif.newX509Data(null); check(false, "null X509Data content"); }
        catch (NullPointerException e) { check(e.getMessage().contains("content"), "null X509Data content message"); }
        try { kif.newX509Data(new ArrayList<Object>()); check(false, "empty X509Data content"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("empty"), "empty X509Data content message"); }
        try { kif.newX509Data(Arrays.asList((Object) "not an X500 name!")); check(false, "invalid subject string"); }
        catch (IllegalArgumentException e) { check(true, "invalid X500 name IAE"); }
        try { kif.newX509Data(Arrays.asList((Object) new Object())); check(false, "invalid content type"); }
        catch (ClassCastException e) { check(e.getMessage() != null, "invalid content type CCE"); }
        // RetrievalMethod
        RetrievalMethod rm1 = kif.newRetrievalMethod("#kk");
        check(rm1.getURI().equals("#kk") && rm1.getType() == null, "RetrievalMethod minimal form uri/type");
        check(rm1.getTransforms() == null || rm1.getTransforms().isEmpty(), "RetrievalMethod no transforms default");
        RetrievalMethod rm2 = kif.newRetrievalMethod("#kk2", "http://www.w3.org/2000/09/xmldsig#KeyValue", null);
        check(rm2.getType().equals("http://www.w3.org/2000/09/xmldsig#KeyValue"), "RetrievalMethod type round-trip");
        try { kif.newRetrievalMethod(null); check(false, "null URI"); }
        catch (NullPointerException e) { check(e.getMessage().contains("uri"), "null URI message"); }
        // KeyInfo assembly
        KeyInfo ki = kif.newKeyInfo(Arrays.asList(kn, kif.newKeyValue(kp.getPublic())), "keyinfo-id");
        check(ki.getId().equals("keyinfo-id"), "KeyInfo id round-trip");
        check(ki.getContent().size() == 2, "KeyInfo content size");
        try { kif.newKeyInfo(null); check(false, "null KeyInfo content"); }
        catch (NullPointerException e) { check(e.getMessage().contains("content"), "null KeyInfo content message"); }
        try { kif.newKeyInfo(Collections.<XMLStructure>emptyList()); check(false, "empty KeyInfo content"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("empty"), "empty KeyInfo content message"); }
        // content list immutable
        try { ki.getContent().add(kn); check(false, "KeyInfo content immutable"); }
        catch (UnsupportedOperationException e) { check(true, "KeyInfo content immutable UOE"); }
    }

    static void testUnmarshalRoundTrip() throws Exception {
        KeyPair kp = rsaKP();
        KeyInfoFactory kif = KeyInfoFactory.getInstance("DOM");
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document doc = newDoc("<r/>");
        KeyInfo ki = kif.newKeyInfo(Arrays.asList(kif.newKeyName("nm"), kif.newKeyValue(kp.getPublic())), "kx");
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
            Collections.singletonList(sf.newReference("#x", sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null))));
        javax.xml.crypto.dsig.XMLObject xo = sf.newXMLObject(null, "x", null, null);
        sf.newXMLSignature(si, ki, Collections.singletonList(xo), null, null)
            .sign(new DOMSignContext(kp.getPrivate(), doc.getDocumentElement()));
        Element kiEl = (Element) doc.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo").item(0);
        check(kiEl.getAttribute("Id").equals("kx"), "marshalled KeyInfo Id attribute");
        KeyInfo back = kif.unmarshalKeyInfo(new DOMStructure(kiEl));
        check(back.getId().equals("kx"), "unmarshalled KeyInfo id round-trip");
        check(back.getContent().size() == 2, "unmarshalled content size");
        check(back.getContent().get(0) instanceof KeyName, "first content is KeyName");
        check(((KeyName) back.getContent().get(0)).getName().equals("nm"), "unmarshalled KeyName value");
        check(back.getContent().get(1) instanceof KeyValue, "second content is KeyValue");
        check(((KeyValue) back.getContent().get(1)).getPublicKey().equals(kp.getPublic()), "unmarshalled KeyValue key equals original");
        // negative: non-KeyInfo element
        try { kif.unmarshalKeyInfo(new DOMStructure(doc.createElement("bogus"))); check(false, "bogus unmarshal"); }
        catch (javax.xml.crypto.MarshalException e) { check(true, "bogus unmarshal MarshalException"); }
    }

    public static void main(String[] args) throws Exception {
        testFactoryMatrix();
        testNewKeyInfoContent();
        testUnmarshalRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
