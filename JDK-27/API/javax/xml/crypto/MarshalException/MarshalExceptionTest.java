/*
 * @test
 * @summary javax.xml.crypto.MarshalException conformance.
 * @run main MarshalExceptionTest
 */

import javax.xml.crypto.MarshalException;

public class MarshalExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(MarshalException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        MarshalException e1 = new MarshalException("m");
        check("m".equals(e1.getMessage()) && e1.getCause() == null, "message ctor");
        Throwable cause = new IllegalStateException("root");
        MarshalException e2 = new MarshalException("m2", cause);
        check("m2".equals(e2.getMessage()) && e2.getCause() == cause, "message+cause ctor");
        MarshalException e3 = new MarshalException(cause);
        check(e3.getCause() == cause && cause.toString().equals(e3.getMessage()), "cause-only ctor");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(e2); }
        MarshalException back = (MarshalException) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check("m2".equals(back.getMessage()), "message survives serialization");
        check(back.getCause() != null && "root".equals(back.getCause().getMessage()), "cause survives serialization");
        // real trigger: unmarshalling a non-Signature element as a signature
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        org.w3c.dom.Element bad = d.createElement("NotASignature");
        try {
            sf.unmarshalXMLSignature(new javax.xml.crypto.dsig.dom.DOMValidateContext(
                javax.xml.crypto.KeySelector.singletonKeySelector(
                    java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic()), bad));
            check(false, "bogus unmarshal should throw");
        } catch (MarshalException me) {
            check(me.getMessage() != null, "real trigger carries a message");
        }
        // real trigger 2: unmarshalKeyInfo from a non-KeyInfo element
        try {
            sf.getKeyInfoFactory().unmarshalKeyInfo(new javax.xml.crypto.dom.DOMStructure(d.createElement("Nope")));
            check(false, "bogus keyinfo unmarshal should throw");
        } catch (MarshalException me2) {
            check(me2.getMessage() != null, "keyinfo trigger carries a message");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
