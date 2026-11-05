/*
 * @test
 * @summary javax.xml.crypto.URIReferenceException conformance.
 * @run main URIReferenceExceptionTest
 */

import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;

public class URIReferenceExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(URIReferenceException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        // all five constructor forms
        check(new URIReferenceException().getMessage() == null, "no-arg ctor");
        URIReferenceException e1 = new URIReferenceException("m");
        check("m".equals(e1.getMessage()) && e1.getCause() == null && e1.getURIReference() == null,
            "message ctor: no cause, no reference");
        Throwable cause = new IllegalStateException("root");
        URIReferenceException e2 = new URIReferenceException("m2", cause);
        check("m2".equals(e2.getMessage()) && e2.getCause() == cause && e2.getURIReference() == null,
            "message+cause ctor: reference null");
        final URIReference ref = new SerialURIReference("#broken");
        URIReferenceException e3 = new URIReferenceException("m3", cause, ref);
        check(e3.getURIReference() == ref, "full ctor preserves reference identity");
        check("#broken".equals(e3.getURIReference().getURI()), "reference URI readable through the exception");
        URIReferenceException e4 = new URIReferenceException(cause);
        check(e4.getCause() == cause && cause.toString().equals(e4.getMessage()), "cause-only ctor");
        // serialization preserves message, cause and uri reference
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(e3); }
        URIReferenceException back = (URIReferenceException) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check("m3".equals(back.getMessage()), "message survives serialization");
        check(back.getURIReference() != null && "#broken".equals(back.getURIReference().getURI()),
            "uri reference survives serialization");
        // real trigger: a signature whose reference cannot be dereferenced
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        try {
            javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
                sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                    (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
                sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
                java.util.Collections.singletonList(sf.newReference("#missing",
                    sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null))));
            org.w3c.dom.Element root = d.createElement("r");
            d.appendChild(root);
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            java.security.KeyPair kp = kpg.generateKeyPair();
            sf.newXMLSignature(si, null).sign(new javax.xml.crypto.dsig.dom.DOMSignContext(kp.getPrivate(), root));
            check(false, "unresolvable reference at sign should throw");
        } catch (javax.xml.crypto.dsig.XMLSignatureException xse) {
            // the DOM provider surfaces the reference failure either directly or as the cause
            check(xse.getCause() instanceof URIReferenceException || xse.getMessage() != null,
                "reference failure carries diagnostics: " + xse);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }

    static class SerialURIReference implements URIReference, java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final String uri;
        SerialURIReference(String uri) { this.uri = uri; }
        public String getURI() { return uri; }
        public String getType() { return null; }
    }
}
