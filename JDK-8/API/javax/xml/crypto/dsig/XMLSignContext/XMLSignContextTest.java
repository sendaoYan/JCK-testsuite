/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignContext conformance.
 * @run main XMLSignContextTest
 */

public class XMLSignContextTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLSignContext");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.XMLCryptoContext"), "extends javax.xml.crypto.XMLCryptoContext");
        // declares no additional members beyond the context contract
        check(c.getDeclaredMethods().length == 0, "no additional declared methods");
        // sole JDK implementation: DOMSignContext
        check(javax.xml.crypto.dsig.dom.DOMSignContext.class
            .getInterfaces()[0].getName().equals("javax.xml.crypto.dsig.XMLSignContext"), "DOMSignContext implements it");
        // a sign context is accepted by XMLSignature.sign
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        org.w3c.dom.Node root = d.createElement("root");
        d.appendChild(root);
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.crypto.SecretKey hmacKey = new javax.crypto.spec.SecretKeySpec(new byte[32], "HmacSHA256");
        javax.xml.crypto.dsig.XMLSignContext ctx = new javax.xml.crypto.dsig.dom.DOMSignContext(hmacKey, root);
        ctx.setBaseURI("http://sign/");
        check("http://sign/".equals(ctx.getBaseURI()), "sign context carries XMLCryptoContext state");
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#hmac-sha256", null),
            java.util.Collections.singletonList(sf.newReference("#o",
                sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null))));
        sf.newXMLSignature(si, null,
            java.util.Collections.singletonList(sf.newXMLObject(null, "o", null, null)), null, null).sign(ctx);
        check(root.getFirstChild().getLocalName().equals("Signature"), "sign(ctx) produced a Signature element");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
