/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignatureException conformance.
 * @run main XMLSignatureExceptionTest
 */

import javax.xml.crypto.dsig.XMLSignatureException;

public class XMLSignatureExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        check(javax.xml.crypto.dsig.XMLSignatureException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        // three constructor forms
        XMLSignatureException e1 = new XMLSignatureException("oops");
        check("oops".equals(e1.getMessage()) && e1.getCause() == null, "message ctor");
        Throwable cause = new IllegalArgumentException("root");
        XMLSignatureException e2 = new XMLSignatureException("oops2", cause);
        check("oops2".equals(e2.getMessage()) && e2.getCause() == cause, "message+cause ctor");
        XMLSignatureException e3 = new XMLSignatureException(cause);
        check(e3.getCause() == cause && cause.toString().equals(e3.getMessage()), "cause-only ctor");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(e2); }
        Object back = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof XMLSignatureException, "serializable");
        check("oops2".equals(((XMLSignatureException) back).getMessage())
            && ((XMLSignatureException) back).getCause() != null
            && ((XMLSignatureException) back).getCause().getMessage().equals("root"),
            "message and cause survive serialization");
        // real trigger: HMACOutputLength below minimum raises it at sign time
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.crypto.SecretKey hmacKey = new javax.crypto.spec.SecretKeySpec(new byte[32], "HmacSHA256");
        try {
            org.w3c.dom.Document dd = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            org.w3c.dom.Node rr = dd.createElement("root");
            dd.appendChild(rr);
            javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
                sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                    (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
                sf.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.HMAC_SHA1,
                    new javax.xml.crypto.dsig.spec.HMACParameterSpec(32)),
                java.util.Collections.singletonList(sf.newReference("#o",
                    sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA1, null))));
            org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            sf.newXMLSignature(si, null,
                java.util.Collections.singletonList(sf.newXMLObject(null, "o", null, null)), null, null)
                .sign(new javax.xml.crypto.dsig.dom.DOMSignContext(hmacKey, rr));
            check(false, "short HMAC output should be rejected");
        } catch (XMLSignatureException xe) {
            check(xe.getMessage() != null && xe.getMessage().contains("HMACOutputLength"), "real trigger names HMACOutputLength");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
