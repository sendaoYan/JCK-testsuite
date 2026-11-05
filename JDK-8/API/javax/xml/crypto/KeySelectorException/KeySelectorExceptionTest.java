/*
 * @test
 * @summary javax.xml.crypto.KeySelectorException conformance.
 * @run main KeySelectorExceptionTest
 */

import javax.xml.crypto.KeySelectorException;

public class KeySelectorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(KeySelectorException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        KeySelectorException e1 = new KeySelectorException("m");
        check("m".equals(e1.getMessage()) && e1.getCause() == null, "message ctor");
        Throwable cause = new IllegalStateException("root");
        KeySelectorException e2 = new KeySelectorException("m2", cause);
        check("m2".equals(e2.getMessage()) && e2.getCause() == cause, "message+cause ctor");
        KeySelectorException e3 = new KeySelectorException(cause);
        check(e3.getCause() == cause, "cause-only ctor");
        check(cause.toString().equals(e3.getMessage()), "cause-only message is cause toString");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(e2); }
        KeySelectorException back = (KeySelectorException) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check("m2".equals(back.getMessage()), "message survives serialization");
        check(back.getCause() != null && "root".equals(back.getCause().getMessage()), "cause survives serialization");
        // real trigger: selector throws during validation -> wrapped as XMLSignatureException with this cause
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        org.w3c.dom.Document d;
        try {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            d = dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8")));
        } catch (Exception e) { throw new RuntimeException(e); }
        java.security.KeyPair kp;
        try {
            java.security.KeyPairGenerator g = java.security.KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            kp = g.generateKeyPair();
        } catch (Exception e) { throw new RuntimeException(e); }
        javax.xml.crypto.dsig.SignedInfo si;
        try {
            si = sf.newSignedInfo(
                sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                    (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
                sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
                java.util.Collections.singletonList(sf.newReference("",
                    sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null),
                    java.util.Collections.singletonList(sf.newTransform(javax.xml.crypto.dsig.Transform.ENVELOPED,
                        (javax.xml.crypto.dsig.spec.TransformParameterSpec) null)), null, null)));
        } catch (Exception e) { throw new RuntimeException(e); }
        try { sf.newXMLSignature(si, null).sign(new javax.xml.crypto.dsig.dom.DOMSignContext(kp.getPrivate(), d.getDocumentElement())); }
        catch (Exception e) { throw new RuntimeException(e); }
        org.w3c.dom.Element sigEl = (org.w3c.dom.Element)
            d.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature").item(0);
        final KeySelectorException kse = new KeySelectorException("no key for you");
        javax.xml.crypto.dsig.dom.DOMValidateContext vc = new javax.xml.crypto.dsig.dom.DOMValidateContext(
            new javax.xml.crypto.KeySelector() {
                public javax.xml.crypto.KeySelectorResult select(javax.xml.crypto.dsig.keyinfo.KeyInfo k,
                        javax.xml.crypto.KeySelector.Purpose p, javax.xml.crypto.AlgorithmMethod m,
                        javax.xml.crypto.XMLCryptoContext x) throws KeySelectorException { throw kse; }
            }, sigEl);
        try {
            sf.unmarshalXMLSignature(vc).validate(vc);
            check(false, "throwing selector must fail validation");
        } catch (javax.xml.crypto.dsig.XMLSignatureException xe) {
            check(xe.getCause() == kse, "KeySelectorException surfaces as cause");
            check(xe.getMessage().contains("cannot find validation key"), "wrapping message");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
