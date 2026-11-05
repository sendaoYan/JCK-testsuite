/*
 * @test
 * @summary javax.xml.crypto.dsig.TransformException conformance: exception hierarchy and constructors.
 * @run main TransformExceptionTest
 */

import javax.xml.crypto.dsig.TransformException;

public class TransformExceptionTest {
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
        check(javax.xml.crypto.dsig.TransformException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");

        // --- constructor chain ---
        TransformException e1 = new TransformException("msg");
        check("msg".equals(e1.getMessage()), "message ctor");
        check(e1.getCause() == null, "message ctor no cause");
        Throwable cause = new IllegalStateException("root");
        TransformException e2 = new TransformException("msg2", cause);
        check("msg2".equals(e2.getMessage()) && e2.getCause() == cause, "message+cause ctor");
        TransformException e3 = new TransformException(cause);
        check(e3.getCause() == cause, "cause-only ctor");
        check(cause.toString().equals(e3.getMessage()), "cause-only ctor message is cause toString");
        // serialization round-trip preserves message and cause
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(e2); }
        Object back = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof TransformException, "serializable");
        check("msg2".equals(((TransformException) back).getMessage()), "message survives serialization");
        check(((TransformException) back).getCause() != null
            && ((TransformException) back).getCause().getMessage().equals("root"),
            "cause survives serialization");
        // quirk (identical on both reference JDKs): the single-arg ctor leaves the cause
        // unset and initCause returns this, yet the overridden getCause() still reports null
        TransformException e4 = new TransformException("m");
        check(e4.getCause() == null, "single-arg ctor cause null");
        check(e4.initCause(cause) == e4, "initCause returns this");
        check(e4.getCause() == null, "overridden getCause() remains null after initCause");

        // --- real trigger: factory-fresh Transform used directly throws TransformException ---
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.xml.crypto.dsig.Transform t = sf.newTransform(javax.xml.crypto.dsig.Transform.BASE64,
            (javax.xml.crypto.dsig.spec.TransformParameterSpec) null);
        try {
            t.transform(new javax.xml.crypto.OctetStreamData(new java.io.ByteArrayInputStream(new byte[0])),
                new javax.xml.crypto.dsig.dom.DOMValidateContext(
                    javax.xml.crypto.KeySelector.singletonKeySelector(java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic()),
                    javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("r")));
            check(false, "direct transform should fail");
        } catch (TransformException te) {
            check(te.getMessage() != null, "real trigger carries a message");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
